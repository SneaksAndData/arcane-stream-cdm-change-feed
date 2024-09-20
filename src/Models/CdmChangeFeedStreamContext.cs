using System;
using System.Text.Json.Serialization;
using Arcane.Framework.Configuration;
using Arcane.Framework.Services.Base;

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
