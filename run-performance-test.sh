#!/bin/bash

# Script shell pour exécuter un test de performance JMeter
# Usage: ./run-performance-test.sh <SCENARIO_JSON> <TEST_RESULT_ID>

set -e

# Configuration
GIT_REPO="https://github.com/malek-soukeh/testmanagement.git"
GIT_BRANCH="master"
BACKEND_URL="http://192.168.56.1:8081"
CALLBACK_TOKEN="your-secret-token-here"

# Paramètres
SCENARIO_JSON="$1"
TEST_RESULT_ID="$2"

if [ -z "$SCENARIO_JSON" ] || [ -z "$TEST_RESULT_ID" ]; then
    echo "Usage: $0 <SCENARIO_JSON> <TEST_RESULT_ID>"
    exit 1
fi

echo "🚀 Démarrage du test de performance..."

# 1. Récupérer template.xml depuis Git
echo "📥 Récupération de template.xml..."
if [ ! -f template.xml ]; then
    git clone --depth 1 -b "$GIT_BRANCH" "$GIT_REPO" temp_repo
    cp temp_repo/template.xml . || exit 1
    rm -rf temp_repo
fi

# 2. Parser le JSON (nécessite jq)
if ! command -v jq &> /dev/null; then
    echo "⚠️  jq non installé, utilisation de python pour parser JSON..."
    # Parser avec python
    PROTOCOL=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json, urllib.parse; data=json.load(sys.stdin); url=urllib.parse.urlparse(data['url']); print(url.scheme)")
    DOMAIN=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json, urllib.parse; data=json.load(sys.stdin); url=urllib.parse.urlparse(data['url']); print(url.netloc.split(':')[0])")
    PORT=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json, urllib.parse; data=json.load(sys.stdin); url=urllib.parse.urlparse(data['url']); print(url.port if url.port else '')")
    PATH=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json, urllib.parse; data=json.load(sys.stdin); url=urllib.parse.urlparse(data['url']); print(url.path if url.path else '/')")
    
    USERS=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('numberOfUsers', 10))")
    RAMP_UP=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('rampUpSeconds', 10))")
    DURATION=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('durationSeconds', 60))")
    TIMEOUT=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('timeoutMs', 5000))")
    REQUESTS_PER_SECOND=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); perf=data.get('performance', {}); print(perf.get('requestsPerSecond', '') if perf.get('requestsPerSecond') else '')")
    
    METHOD=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('additionalParams', {}).get('method', 'GET'))")
    AUTHORIZATION=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('additionalParams', {}).get('authorization', ''))")
    CONTENT_TYPE=$(echo "$SCENARIO_JSON" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('performance', {}).get('additionalParams', {}).get('contentType', 'application/json'))")
else
    # Parser avec jq (plus simple)
    PROTOCOL=$(echo "$SCENARIO_JSON" | jq -r '.url | split("://")[0]')
    DOMAIN=$(echo "$SCENARIO_JSON" | jq -r '.url | split("://")[1] | split("/")[0] | split(":")[0]')
    PORT=$(echo "$SCENARIO_JSON" | jq -r '.url | split("://")[1] | split("/")[0] | split(":")[1] // empty')
    PATH=$(echo "$SCENARIO_JSON" | jq -r '.url | split("://")[1] | sub("^[^/]*"; "") | if . == "" then "/" else . end')
    
    USERS=$(echo "$SCENARIO_JSON" | jq -r '.performance.numberOfUsers // 10')
    RAMP_UP=$(echo "$SCENARIO_JSON" | jq -r '.performance.rampUpSeconds // 10')
    DURATION=$(echo "$SCENARIO_JSON" | jq -r '.performance.durationSeconds // 60')
    TIMEOUT=$(echo "$SCENARIO_JSON" | jq -r '.performance.timeoutMs // 5000')
    REQUESTS_PER_SECOND=$(echo "$SCENARIO_JSON" | jq -r '.performance.requestsPerSecond // empty')
    
    METHOD=$(echo "$SCENARIO_JSON" | jq -r '.performance.additionalParams.method // "GET"')
    AUTHORIZATION=$(echo "$SCENARIO_JSON" | jq -r '.performance.additionalParams.authorization // ""')
    CONTENT_TYPE=$(echo "$SCENARIO_JSON" | jq -r '.performance.additionalParams.contentType // "application/json"')
fi

echo "Paramètres extraits:"
echo "  URL: $PROTOCOL://$DOMAIN${PORT:+:$PORT}$PATH"
echo "  Users: $USERS"
echo "  Ramp-up: ${RAMP_UP}s"
echo "  Duration: ${DURATION}s"
echo "  Timeout: ${TIMEOUT}ms"

# 3. Créer le répertoire de résultats
mkdir -p results
RESULT_FILE="results/result_$(date +%s).jtl"

# 4. Exécuter JMeter
echo "🚀 Exécution de JMeter..."
jmeter -n -t template.xml \
    -JUSERS="$USERS" \
    -JRAMP_UP="$RAMP_UP" \
    -JDURATION="$DURATION" \
    -JUSE_DURATION=true \
    -JPROTOCOL="$PROTOCOL" \
    -JDOMAIN="$DOMAIN" \
    -JPORT="${PORT:-}" \
    -JPATH="$PATH" \
    -JMETHOD="$METHOD" \
    -JAUTHORIZATION="$AUTHORIZATION" \
    -JCONTENT_TYPE="$CONTENT_TYPE" \
    -JTIMEOUT="$TIMEOUT" \
    -JREQUESTS_PER_SECOND="${REQUESTS_PER_SECOND:-}" \
    -JRESULT_FILE="$RESULT_FILE" \
    -l "$RESULT_FILE"

if [ ! -f "$RESULT_FILE" ]; then
    echo "❌ ERREUR: Fichier de résultats non créé!"
    exit 1
fi

echo "✅ Test terminé. Fichier: $RESULT_FILE"

# 5. Parser les résultats (script Python simple)
echo "📊 Parsing des résultats..."
METRICS=$(python3 <<EOF
import csv
import sys

result_file = "$RESULT_FILE"
response_times = []
errors = 0
total = 0

try:
    with open(result_file, 'r') as f:
        reader = csv.reader(f)
        for row in reader:
            if len(row) < 2 or row[0] == 'timeStamp':
                continue
            total += 1
            try:
                elapsed = int(row[1])
                response_times.append(elapsed)
                if len(row) > 7 and row[7] == 'false':
                    errors += 1
            except:
                pass
    
    if response_times:
        response_times.sort()
        avg = round(sum(response_times) / len(response_times))
        max_time = max(response_times)
        p95 = response_times[int(len(response_times) * 0.95)]
        error_rate = round((errors / total) * 100 * 100) / 100 if total > 0 else 100.0
        status = "PASSED" if error_rate < 5 else "FAILED"
        
        print(f"{avg},{max_time},{p95},{error_rate},{status}")
    else:
        print("0,0,0,100.0,FAILED")
except Exception as e:
    print(f"0,0,0,100.0,FAILED")
    sys.stderr.write(f"Error: {e}\n")
EOF
)

IFS=',' read -r AVG MAX P95 ERROR_RATE STATUS <<< "$METRICS"

echo "Métriques:"
echo "  Avg: ${AVG}ms"
echo "  Max: ${MAX}ms"
echo "  P95: ${P95}ms"
echo "  Error Rate: ${ERROR_RATE}%"
echo "  Status: $STATUS"

# 6. Envoyer le callback
echo "📤 Envoi du callback au backend..."
CALLBACK_PAYLOAD=$(cat <<EOF
{
  "status": "$STATUS",
  "avgResponseTimeMs": $AVG,
  "maxResponseTimeMs": $MAX,
  "p95ResponseTimeMs": $P95,
  "errorRatePercent": $ERROR_RATE
}
EOF
)

curl -X POST \
    "${BACKEND_URL}/api/performance/results/${TEST_RESULT_ID}/callback" \
    -H "Content-Type: application/json" \
    -H "X-JENKINS-TOKEN: ${CALLBACK_TOKEN}" \
    -d "$CALLBACK_PAYLOAD"

echo ""
echo "✅ Callback envoyé avec succès!"


