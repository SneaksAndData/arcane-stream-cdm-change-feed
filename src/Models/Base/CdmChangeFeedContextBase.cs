using Arcane.Framework.Services.Base;

namespace Arcane.Stream.Cdm.Models.Base;

public abstract class CdmChangeFeedContextBase : IStreamContext, IStreamContextWriter
{
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

    public abstract CdmChangeFeedContextBase LoadSecrets();
}
