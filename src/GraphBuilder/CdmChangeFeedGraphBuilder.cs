using System;
using System.Threading.Tasks;
using Akka.Streams;
using Akka.Streams.Dsl;
using Arcane.Framework.Services.Base;
using Arcane.Framework.Sources.CdmChangeFeedSource;
using Arcane.Stream.Cdm.Extensions;
using Arcane.Stream.Cdm.Models;
using Snd.Sdk.Metrics.Base;
using Snd.Sdk.Storage.Base;

namespace Arcane.Stream.Cdm;

public class CdmChangeFeedGraphBuilder : IStreamGraphBuilder<IStreamContext>
{
    private readonly IBlobStorageService blobStorageService;
    private readonly MetricsService metricsService;

    public CdmChangeFeedGraphBuilder(IBlobStorageService blobStorageService, MetricsService metricsService)
    {
        this.blobStorageService = blobStorageService;
        this.metricsService = metricsService;
    }

    public IRunnableGraph<(UniqueKillSwitch, Task)> BuildGraph(IStreamContext context)
    {
        return context switch
        {
            CdmChangeFeedStreamContext configuration => this.GetSource(configuration).BuildGraph(this.metricsService,
                this.blobStorageService, context, configuration.SinkLocation, configuration.RowsPerGroup,
                configuration.GroupsPerFile, configuration.GroupingInterval),
            _ => throw new System.NotImplementedException()
        };
    }

    private CdmChangeFeedSource GetSource(CdmChangeFeedStreamContext context)
    {
        return CdmChangeFeedSource.Create(context.BaseLocation, context.EntityName, this.blobStorageService,
            context.IsBackfilling,
            context.ChangeCaptureInterval, context.IsBackfilling, TimeSpan.FromSeconds(10));
    }
}
