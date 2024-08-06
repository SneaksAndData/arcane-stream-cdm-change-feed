{{/*
Expand the name of the chart.
*/}}
{{- define "app.name" -}}
{{- default .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "app.fullname" -}}
{{- $name := .Chart.Name }}
{{- if contains .Release.Name $name}}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "app.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "app.labels" -}}
helm.sh/chart: {{ include "app.chart" . }}
{{ include "app.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.jobTemplateSettings.additionalLabels }}
{{ toYaml . }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "app.selectorLabels" -}}
app.kubernetes.io/name: {{ include "app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "app.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "app.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Generage image reference based on image repository and tag
*/}}
{{- define "app.image" -}}
{{- printf "%s:%s" .Values.image.repository  (default (printf "v%s" .Chart.AppVersion) .Values.image.tag) }}
{{- end }}

{{/*
Stream class labels
*/}}
{{- define "streamclass.labels" -}}
helm.sh/chart: {{ include "app.chart" . }}
{{ include "app.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.additionalLabels }}
{{ toYaml . }}
{{- end }}
{{- end }}

{{/*
Generate the job editor cluster role name
*/}}
{{- define "app.clusteRole.jobEditor" -}}
{{- if .Values.rbac.clusterRole.jobEditor.nameOverride }}
{{- .Values.rbac.clusterRole.jobEditor.nameOverride }}
{{- else }}
{{- printf "%s-job-editor" (include "app.fullname" .) }}
{{- end }}
{{- end }}

{{/*
Generate the CR viewer cluster role name
*/}}
{{- define "app.clusteRole.cdmStreamViewer" -}}
{{- if .Values.rbac.clusterRole.cdmStreamViewer.nameOverride }}
{{- .Values.rbac.clusterRole.cdmStreamViewer.nameOverride }}
{{- else }}
{{- printf "%s-cdm-change-feed-viewer" (include "app.fullname" .) }}
{{- end }}
{{- end }}

{{/*
Generate the CR editor cluster role name
*/}}
{{- define "app.clusteRole.cdmStreamEditorr" -}}
{{- if .Values.rbac.clusterRole.cdmStreamEditor.nameOverride }}
{{- .Values.rbac.clusterRole.cdmStreamEditor.nameOverride }}
{{- else }}
{{- printf "%s-cdm-change-feed-editor" (include "app.fullname" .) }}
{{- end }}
{{- end }}

{{/*
Job template standard labels
*/}}
{{- define "job.labels" -}}
helm.sh/chart: {{ include "app.chart" . }}
{{ include "app.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- with .Values.jobTemplateSettings.additionalLabels }}
{{ toYaml . }}
{{- end }}
{{- end }}
