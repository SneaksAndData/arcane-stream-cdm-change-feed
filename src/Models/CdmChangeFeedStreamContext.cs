using System;
using Arcane.Framework.Sources.CdmChangeFeedSource;
using Arcane.Stream.Cdm.Models.Base;

namespace Arcane.Stream.Cdm.Models;

public class CdmChangeFeedStreamContext : CdmChangeFeedContextBase
{
    /// <summary>
    /// Connection string for the datalake storage account containing CDM entities.
    /// </summary>
    public string ConnectionString { get; set; }

    /// <summary>
    /// Max number of retries on blob reads for the http client.
    /// </summary>
    public int HttpClientMaxRetries { get; set; }

    /// <summary>
    /// Max retry delay on blob reads for the http client.
    /// </summary>
    public TimeSpan HttpClientRetryDelay { get; set; }

    /// <summary>
    /// Location root for CDM entities.
    /// </summary>
    public string BaseLocation { get; set; }

    /// <summary>
    /// Name of a CDM entity to stream.
    /// </summary>
    public string EntityName { get; set; }

    /// <summary>
    /// Interval between scans when no new data is found.
    /// </summary>
    public TimeSpan ChangeCaptureInterval { get; set; }

    /// <summary>
    /// Number of rows per parquet rowgroup.
    /// </summary>
    public int RowsPerGroup { get; set; }

    /// <summary>
    /// Max time to wait for rowsPerGroup to accumulate.
    /// </summary>
    public TimeSpan GroupingInterval { get; set; }

    /// <summary>
    /// Number of row groups per file.
    /// </summary>
    public int GroupsPerFile { get; set; }

    /// <summary>
    /// Data location for parquet files.
    /// </summary>
    public string SinkLocation { get; set; }

    public override CdmChangeFeedContextBase LoadSecrets()
    {
        return this;
    }
}
