using System;
using Arcane.Framework.Contracts;
using Arcane.Framework.Providers.Hosting;
using Arcane.Stream.Cdm;
using Arcane.Stream.Cdm.Models;
using Microsoft.Extensions.Hosting;
using Serilog;
using Snd.Sdk.Logs.Providers;
using Snd.Sdk.Metrics.Configurations;
using Snd.Sdk.Metrics.Providers;
using Snd.Sdk.Storage.Providers;
using Snd.Sdk.Storage.Providers.Configurations;

Log.Logger = DefaultLoggingProvider.CreateBootstrapLogger(nameof(Arcane));

int exitCode;
try
{
    Console.WriteLine(AppDomain.CurrentDomain.FriendlyName.ToUpperInvariant());
    exitCode = await Host.CreateDefaultBuilder(args)
        .AddDatadogLogging((_, _, configuration) => configuration.EnrichWithCustomProperties().WriteTo.Console())
        .ConfigureRequiredServices(services =>
            services.AddStreamGraphBuilder<CdmChangeFeedGraphBuilder, CdmChangeFeedStreamContext>())
        .ConfigureAdditionalServices(
            (services, context) =>
            {
                services.AddAzureBlob(AzureStorageConfiguration.CreateDefault());
                services.AddDatadogMetrics(
                    configuration: DatadogConfiguration.UnixDomainSocket(context.ApplicationName));
                services.AddAwsS3Writer(AmazonStorageConfiguration.CreateFromEnv());
            })
        .Build()
        .RunStream<CdmChangeFeedStreamContext>(Log.Logger);
}
catch (Exception ex)
{
    Log.Fatal(ex, "Host terminated unexpectedly");
    return ExitCodes.FATAL;
}
finally
{
    await Log.CloseAndFlushAsync();
}

return exitCode;
