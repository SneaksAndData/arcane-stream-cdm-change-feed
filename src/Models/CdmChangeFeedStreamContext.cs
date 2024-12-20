using System;
using System.Text.Json.Serialization;
using Akka.Util;
using Arcane.Framework.Configuration;
using Arcane.Framework.Services.Base;
using Arcane.Framework.Sinks.Models;

namespace Arcane.Stream.Cdm.Models;

public class CdmChangeFeedStreamContext : IStreamContext, IStreamContextWriter
{
    /// <summary>
    /// Location root for CDM entities.
    /// </summary>
    public string BaseLocation { get; set; }

    /// <summary>
    /// Name of a CDM entity to stream.
    /// </summary>
    public string EntityName { get; set; }

    /// <summary>
    /// How often to check for changes in the source cdm change feed .
    /// </summary>
    [JsonConverter(typeof(SecondsToTimeSpanConverter))]
    [JsonPropertyName("changeCaptureIntervalSeconds")]
    public TimeSpan ChangeCaptureInterval { get; set; }

    /// <summary>
    /// Number of rows per parquet rowgroup.
    /// </summary>
    public int RowsPerGroup { get; set; }

    /// <summary>
    /// Max time to wait for rowsPerGroup to accumulate.
    /// </summary>
    [JsonConverter(typeof(SecondsToTimeSpanConverter))]
    [JsonPropertyName("groupingIntervalSeconds")]
    public TimeSpan GroupingInterval { get; set; }

    /// <summary>
    /// Number of row groups per file.
    /// </summary>
    public int GroupsPerFile { get; set; }

    /// <summary>
    /// Data location for parquet files.
    /// </summary>
    public string SinkLocation { get; set; }
    
    /// <summary>
    /// Number of seconds to look back when determining first set of changes to extract.
    /// </summary>
    public int LookbackInterval { get; set; }
    
    /// <summary>
    /// How often to check for changes in the source cdm change feed schema.
    /// </summary>
    [JsonConverter(typeof(SecondsToTimeSpanConverter))]
    [JsonPropertyName("schemaUpdateIntervalSeconds")]
    public TimeSpan SchemaUpdateInterval { get; set; }

    public Option<StreamMetadata> GetStreamMetadata() => new StreamMetadata(Option<StreamPartition[]>.None);

    /// <inheritdoc cref="IStreamContext.StreamId"/>>
    public string StreamId { get; private set; }

    /// <inheritdoc cref="IStreamContext.StreamKind"/>>
    public string StreamKind { get; private set; }

    /// <inheritdoc cref="IStreamContext.IsBackfilling"/>>
    public bool IsBackfilling { get; private set; }

    /// <inheritdoc cref="IStreamContextWriter.SetStreamId"/>>
    public void SetStreamId(string streamId)
    {
        this.StreamId = streamId;
    }

    /// <inheritdoc cref="IStreamContextWriter.SetBackfilling"/>>
    public void SetBackfilling(bool isRunningInBackfillMode)
    {
        this.IsBackfilling = isRunningInBackfillMode;
    }

    /// <inheritdoc cref="IStreamContextWriter.SetStreamKind"/>>
    public void SetStreamKind(string streamKind)
    {
        this.StreamKind = streamKind;
    }
}
