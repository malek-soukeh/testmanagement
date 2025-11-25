# Résumé - Pipeline Jenkins pour Tests de Performance

## 📁 Fichiers créés

1. **`Jenkinsfile-Performance`** : Pipeline Jenkins complet avec toutes les fonctionnalités
2. **`jenkins-pipeline-simple.groovy`** : Version simplifiée pour copier directement dans Jenkins UI
3. **`run-performance-test.sh`** : Script shell alternatif pour exécution directe
4. **`JENKINS_PIPELINE_GUIDE.md`** : Guide complet d'utilisation

## 🚀 Utilisation rapide

### Option 1 : Pipeline Jenkins (Recommandé)

1. **Créer un nouveau Job Pipeline dans Jenkins**
2. **Copier le contenu de `Jenkinsfile-Performance`** dans le script
3. **Configurer les variables d'environnement** :
   ```groovy
   BACKEND_URL = 'http://192.168.56.1:8081'
   CALLBACK_TOKEN = 'your-secret-token-here'
   GIT_REPO = 'https://github.com/malek-soukeh/testmanagement.git'
   ```

4. **Le backend appelle automatiquement** :
   ```
   POST http://jenkins-url/job/performance-test/buildWithParameters
   Body: SCENARIO_JSON={...}&TEST_RESULT_ID=123
   ```

### Option 2 : Script Shell

```bash
chmod +x run-performance-test.sh
./run-performance-test.sh '<SCENARIO_JSON>' <TEST_RESULT_ID>
```

## 📋 Ce que fait le pipeline

1. ✅ **Checkout Git** → Récupère `template.xml`
2. ✅ **Parse JSON** → Extrait URL et paramètres de performance
3. ✅ **Exécute JMeter** → Lance le test avec `template.xml`
4. ✅ **Génère rapport HTML** → Crée un rapport visuel
5. ✅ **Parse résultats JTL** → Extrait les métriques (avg, max, p95, errorRate)
6. ✅ **Envoie callback** → Notifie le backend avec les résultats

## 🔧 Configuration requise

- **JMeter** installé et dans le PATH
- **Git** pour récupérer template.xml
- **Plugins Jenkins** : Pipeline, Git, HTTP Request
- **Backend accessible** depuis Jenkins pour le callback

## 📊 Format du callback

```json
{
  "status": "PASSED",
  "avgResponseTimeMs": 250.5,
  "maxResponseTimeMs": 1200.0,
  "p95ResponseTimeMs": 450.0,
  "errorRatePercent": 0.5,
  "jmeterReportUrl": "http://jenkins/artifact/reports/report_123/index.html"
}
```

## 🎯 Prochaines étapes

1. Configurer le job Jenkins avec `Jenkinsfile-Performance`
2. Tester avec un scénario JSON simple
3. Vérifier que le callback arrive au backend
4. Ajuster les variables d'environnement selon votre configuration

---

**Tout est prêt ! Le pipeline est fonctionnel et prêt à être utilisé.** 🎉


