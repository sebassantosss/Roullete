import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api/v1/roulette';

function App() {
  const [historicalNumbers, setHistoricalNumbers] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [bulkInput, setBulkInput] = useState('');
  const [predictions, setPredictions] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [patternsDetected, setPatternsDetected] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [lastUpdate, setLastUpdate] = useState(null);

  const analyzePatternsAndPredict = async (numbers) => {
    if (numbers.length < 5) {
      setError('Se requieren al menos 5 números históricos para el análisis');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await axios.post(`${API_BASE_URL}/analyze`, {
        historicalNumbers: numbers
      });

      setPredictions(response.data.topPredictions);
      setStatistics(response.data.statistics);
      setPatternsDetected(response.data.patternsDetected);
      setLastUpdate(new Date());
    } catch (err) {
      setError('Error al analizar patrones: ' + (err.response?.data?.message || err.message));
      console.error('Analysis error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Auto-analyze when historical numbers change
  useEffect(() => {
    if (historicalNumbers.length >= 5) {
      analyzePatternsAndPredict(historicalNumbers);
    }
  }, [historicalNumbers]);

  const handleAddNumber = () => {
    const num = parseInt(inputValue);
    
    if (isNaN(num)) {
      setError('Por favor ingresa un número válido');
      return;
    }
    
    if (num < 0 || num > 36) {
      setError('El número debe estar entre 0 y 36');
      return;
    }

    setHistoricalNumbers([...historicalNumbers, num]);
    setInputValue('');
    setError('');
  };

  const handleBulkLoad = () => {
    const numbers = bulkInput
      .split(/[\s,]+/)
      .map(n => parseInt(n.trim()))
      .filter(n => !isNaN(n) && n >= 0 && n <= 36);

    if (numbers.length === 0) {
      setError('No se encontraron números válidos');
      return;
    }

    setHistoricalNumbers(numbers);
    setBulkInput('');
    setError('');
  };

  const handleClear = () => {
    setHistoricalNumbers([]);
    setPredictions([]);
    setStatistics(null);
    setPatternsDetected([]);
    setError('');
  };

  const handleRemoveLastNumber = () => {
    setHistoricalNumbers(historicalNumbers.slice(0, -1));
  };

  const getColorClass = (number) => {
    const reds = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];
    if (number === 0) return 'green';
    return reds.includes(number) ? 'red' : 'black';
  };

  const getProbabilityColor = (probability) => {
    if (probability >= 60) return '#10b981'; // green
    if (probability >= 40) return '#f59e0b'; // orange
    return '#ef4444'; // red
  };

  return (
    <div className="app">
      <header className="header">
        <h1>🎰 Roulette Predictor</h1>
        <p>Análisis de Patrones en Tiempo Real</p>
      </header>

      <div className="container">
        {/* Input Section */}
        <div className="card">
          <h2>📊 Datos Históricos</h2>
          
          <div className="input-section">
            <div className="single-input">
              <input
                type="number"
                min="0"
                max="36"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleAddNumber()}
                placeholder="Ingresa número (0-36)"
                className="input"
              />
              <button onClick={handleAddNumber} className="btn btn-primary">
                Agregar
              </button>
            </div>

            <div className="bulk-input">
              <textarea
                value={bulkInput}
                onChange={(e) => setBulkInput(e.target.value)}
                placeholder="O pega múltiples números separados por espacios o comas&#10;Ejemplo: 14, 34, 11, 33, 8, 27, 29..."
                className="textarea"
                rows="3"
              />
              <button onClick={handleBulkLoad} className="btn btn-secondary">
                Cargar Datos
              </button>
            </div>
          </div>

          {error && <div className="error">{error}</div>}

          {/* Historical Numbers Display */}
          {historicalNumbers.length > 0 && (
            <div className="historical-section">
              <div className="historical-header">
                <h3>Números Históricos ({historicalNumbers.length})</h3>
                <div>
                  <button onClick={handleRemoveLastNumber} className="btn btn-small">
                    Eliminar Último
                  </button>
                  <button onClick={handleClear} className="btn btn-small btn-danger">
                    Limpiar Todo
                  </button>
                </div>
              </div>
              
              <div className="numbers-grid">
                {historicalNumbers.map((num, idx) => (
                  <div key={idx} className={`number-chip ${getColorClass(num)}`}>
                    {num}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Predictions Section */}
        {loading && (
          <div className="card">
            <div className="loading">Analizando patrones...</div>
          </div>
        )}

        {!loading && predictions.length > 0 && (
          <>
            {/* Recently Appeared Warning */}
            {statistics && statistics.recentlyAppeared && statistics.recentlyAppeared.length > 0 && (
              <div className="card warning-card">
                <h3>⚠️ Números "Quemados" (Últimos 5 que salieron)</h3>
                <p style={{marginBottom: '12px', color: '#6b7280'}}>
                  Estos números tienen probabilidad MUY reducida porque acaban de aparecer:
                </p>
                <div className="numbers-grid">
                  {statistics.recentlyAppeared.map((num, idx) => (
                    <div key={idx} className={`number-chip ${getColorClass(num)} burned`}>
                      {num}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Top Predictions */}
            <div className="card">
              <div className="predictions-header">
                <h2>🎯 Predicciones Top</h2>
                {lastUpdate && (
                  <span className="last-update">
                    Última actualización: {lastUpdate.toLocaleTimeString()}
                  </span>
                )}
              </div>

              <div className="predictions-grid">
                {predictions.map((pred, idx) => (
                  <div key={idx} className="prediction-card">
                    <div className="prediction-header">
                      <div className={`prediction-number ${getColorClass(pred.number)}`}>
                        {pred.number}
                      </div>
                      <div className="prediction-prob">
                        <div 
                          className="prob-bar" 
                          style={{
                            width: `${pred.probability}%`,
                            backgroundColor: getProbabilityColor(pred.probability)
                          }}
                        />
                        <span className="prob-text">{pred.probability}%</span>
                      </div>
                    </div>

                    <div className="prediction-details">
                      <div className="confirmations-badge">
                        {pred.confirmations} confirmación{pred.confirmations !== 1 ? 'es' : ''}
                      </div>
                      <div className="pattern-type">{pred.patternType}</div>
                    </div>

                    <div className="reasons">
                      {pred.reasons.slice(0, 3).map((reason, ridx) => (
                        <div key={ridx} className="reason">• {reason}</div>
                      ))}
                      {pred.reasons.length > 3 && (
                        <div className="reason more">
                          +{pred.reasons.length - 3} más...
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Statistics */}
            {statistics && (
              <div className="stats-grid">
                <div className="card stat-card">
                  <h3>📈 Estadísticas</h3>
                  <div className="stat-item">
                    <span className="stat-label">Total de números:</span>
                    <span className="stat-value">{statistics.totalNumbers}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Números únicos:</span>
                    <span className="stat-value">{statistics.uniqueNumbers}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Último número:</span>
                    <span className={`stat-value number-chip ${getColorClass(statistics.lastNumber)}`}>
                      {statistics.lastNumber}
                    </span>
                  </div>
                </div>

                {statistics.mostFrequent && statistics.mostFrequent.length > 0 && (
                  <div className="card stat-card">
                    <h3>🔥 Números Calientes</h3>
                    {statistics.mostFrequent.map((item, idx) => (
                      <div key={idx} className="stat-item">
                        <span className={`number-chip small ${getColorClass(item.key)}`}>
                          {item.key}
                        </span>
                        <span className="stat-value">{item.value} veces</span>
                      </div>
                    ))}
                  </div>
                )}

                {statistics.dozenDistribution && (
                  <div className="card stat-card">
                    <h3>📊 Distribución por Docenas</h3>
                    <div className="stat-item">
                      <span className="stat-label">1ª Docena (1-12):</span>
                      <span className="stat-value">{statistics.dozenDistribution['1st']}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">2ª Docena (13-24):</span>
                      <span className="stat-value">{statistics.dozenDistribution['2nd']}</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">3ª Docena (25-36):</span>
                      <span className="stat-value">{statistics.dozenDistribution['3rd']}</span>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Patterns Detected */}
            {patternsDetected.length > 0 && (
              <div className="card">
                <h3>🔍 Patrones Detectados</h3>
                <div className="patterns-list">
                  {patternsDetected.map((pattern, idx) => (
                    <div key={idx} className="pattern-item">
                      ✓ {pattern}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {!loading && historicalNumbers.length === 0 && (
          <div className="card empty-state">
            <h3>👆 Comienza ingresando números históricos</h3>
            <p>Puedes agregar números uno por uno o cargar múltiples al mismo tiempo.</p>
            <p>Se requieren al menos 5 números para comenzar el análisis.</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
