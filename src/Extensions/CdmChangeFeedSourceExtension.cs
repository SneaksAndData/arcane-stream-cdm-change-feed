using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Akka.Streams;
using Akka.Streams.Dsl;
using Arcane.Framework.Contracts;
using Arcane.Framework.Services.Base;
using Arcane.Framework.Sinks.Parquet;
using Arcane.Framework.Sources.CdmChangeFeedSource;
using Parquet.Data;
using Snd.Sdk.Metrics.Base;
using Snd.Sdk.Storage.Base;

namespace Arcane.Stream.Cdm.Extensions;

public static class CdmChangeFeedSourceExtension
{
    public static IRunnableGraph<(UniqueKillSwitch, Task)> BuildGraph(this CdmChangeFeedSource source,
        MetricsService metricsService, IBlobStorageWriter blobStorageWriter, IStreamContext context,
        string sinkLocation, int rowsPerGroup, int groupsPerFile, TimeSpan groupingInterval)
    {
        var dimensions = source.GetDefaultTags().GetAsDictionary(context, context.StreamId);
        var parquetSink =
            context.ParquetSinkFromContext(source.GetParquetSchema(), blobStorageWriter, sinkLocation, groupsPerFile);
        return Source.FromGraph(source)
            .GroupedWithin(rowsPerGroup, groupingInterval)
            .Select(grp =>
            {
                var rows = grp.ToList();
                metricsService.Increment(DeclaredMetrics.ROWS_INCOMING, dimensions, rows.Count);
                return rows;
            }).Select(rows => rows.AsRowGroup(source.GetParquetSchema()))
            .Log(context.StreamKind)
            .ViaMaterialized(KillSwitches.Single<List<DataColumn>>(), Keep.Right)
            .ToMaterialized(parquetSink, Keep.Both);
    }

    private static ParquetSink ParquetSinkFromContext(this IStreamContext streamContext, Schema schema,
        IBlobStorageWriter blobStorageWriter, string sinkLocation, int groupsPerFile)
    {
        var parquetSink = ParquetSink.Create(schema, blobStorageWriter, $"{sinkLocation}/{streamContext.StreamId}",
            groupsPerFile, true, false, "data", "schema", streamContext.IsBackfilling);
        return parquetSink;
    }
}
