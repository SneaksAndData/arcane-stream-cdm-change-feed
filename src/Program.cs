using System;
using System.Reflection;
using Arcane.Framework.Contracts;
using Arcane.Framework.Providers;
using Arcane.Framework.Providers.Hosting;
using Arcane.Stream.Cdm;
using Arcane.Stream.Cdm.Models;
using Arcane.Stream.Cdm.Models.Base;
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
    exitCode = await Host.CreateDefaultBuilder(args).AddDatadogLogging((_, _, configuration) => configuration.WriteTo.Console())
        .ConfigureRequiredServices(services =>
        {
            return services.AddStreamGraphBuilder<CdmChangeFeedGraphBuilder>(context =>
            {
                var contextNS = typeof(CdmChangeFeedStreamContext).Namespace;
                var typeFullName = $"{contextNS}.{context.StreamKind}StreamContext";
                var targetType = Assembly.GetExecutingAssembly().GetType(typeFullName);
                if (targetType is null)
                {
                    throw new ArgumentException($"Unknown stream kind {typeFullName}. Cannot load stream context.");
                }
                return ((CdmChangeFeedContextBase)StreamContext.ProvideFromEnvironment(targetType)).LoadSecrets();

            });
        })
        .ConfigureAdditionalServices((services, context) =>
        {
            services.AddAzureBlob(AzureStorageConfiguration.CreateDefault());
            services.AddDatadogMetrics(configuration: DatadogConfiguration.UnixDomainSocket(context.ApplicationName));
            services.AddAwsS3Writer(AmazonStorageConfiguration.CreateFromEnv());
        })
        .Build()

        .RunStream(Log.Logger);
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
