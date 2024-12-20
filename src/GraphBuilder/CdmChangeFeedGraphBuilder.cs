using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Akka.Streams;
using Akka.Streams.Dsl;
using Akka.Util;
using Arcane.Framework.Contracts;
using Arcane.Framework.Services.Base;
using Arcane.Framework.Sinks.Models;
using Arcane.Framework.Sinks.Parquet;
using Arcane.Framework.Sources.CdmChangeFeedSource;
using Arcane.Stream.Cdm.Models;
using Parquet.Data;
using Snd.Sdk.Metrics.Base;
using Snd.Sdk.Storage.Base;

namespace Arcane.Stream.Cdm.GraphBuilder;

public class CdmChangeFeedGraphBuilder : IStreamGraphBuilder<CdmChangeFeedStreamContext>
{
    private readonly IBlobStorageService blobStorageService;
    private readonly MetricsService metricsService;
    private readonly IBlobStorageWriter blobStorageWriter;
    private readonly IInterruptionToken interruptionToken;

    public CdmChangeFeedGraphBuilder(IBlobStorageService blobStorageService, MetricsService metricsService,
        IBlobStorageWriter blobStorageWriter, IInterruptionToken interruptionToken)
    {
        this.blobStorageService = blobStorageService;
        this.metricsService = metricsService;
        this.blobStorageWriter = blobStorageWriter;
        this.interruptionToken = interruptionToken;
    }

    public IRunnableGraph<(UniqueKillSwitch, Task)> BuildGraph(CdmChangeFeedStreamContext context)
    {
        var source = CdmChangeFeedSource.Create(context.BaseLocation, context.EntityName, this.blobStorageService,
            context.IsBackfilling,
            context.ChangeCaptureInterval,
            context.IsBackfilling,
            context.LookbackInterval, 
            context.SchemaUpdateInterval);

        var dimensions = source.GetDefaultTags().GetAsDictionary(context, context.StreamId);
        var parquetSink = ParquetSinkFromContext(context,
            source.GetParquetSchema(),
            this.blobStorageWriter,
            context.SinkLocation,
            this.interruptionToken);
        return Source.FromGraph(source)
            .GroupedWithin(context.RowsPerGroup, context.GroupingInterval)
            .Select(grp =>
            {
                var rows = grp.ToList();
                this.metricsService.Increment(DeclaredMetrics.ROWS_INCOMING, dimensions, rows.Count);
                return rows;
            })
            .Select(rows => rows.AsRowGroup(source.GetParquetSchema()))
            .Log(context.StreamKind)
            .ViaMaterialized(KillSwitches.Single<List<DataColumn>>(), Keep.Right)
            .ToMaterialized(parquetSink, Keep.Both);
    }


    private static ParquetSink ParquetSinkFromContext(CdmChangeFeedStreamContext streamContext, Schema schema,
        IBlobStorageWriter blobStorageWriter, string sinkLocation, IInterruptionToken interruptionToken)
    {
        var parquetSink = ParquetSink.Create(parquetSchema: schema, storageWriter: blobStorageWriter,
            parquetFilePath: $"{sinkLocation}/{streamContext.StreamId}",
            rowGroupsPerFile: streamContext.GroupsPerFile,
            createSchemaFile: true,
            interruptionToken: interruptionToken,
            streamMetadata: streamContext.GetStreamMetadata().GetOrElse(new StreamMetadata(Option<StreamPartition[]>.None)),
            dataSinkPathSegment: streamContext.IsBackfilling ? "backfill" : "data",
            dropCompletionToken: streamContext.IsBackfilling);

        return parquetSink;
    }
}
