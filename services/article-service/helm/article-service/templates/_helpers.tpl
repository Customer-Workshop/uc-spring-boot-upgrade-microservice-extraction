{{- define "chart.fullname" -}}
{{ .Release.Name }}-article-service
{{- end }}

{{- define "chart.labels" -}}
app.kubernetes.io/name: article-service
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "chart.selectorLabels" -}}
app.kubernetes.io/name: article-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
