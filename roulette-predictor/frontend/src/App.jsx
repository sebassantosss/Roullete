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
  
  // Dozen analysis state
  const [dozenAnalysis, setDozenAnalysis] = useState(null);
  const [showDozenView, setShowDozenView] = useState(true);
  const [showHistoricalNumbers, setShowHistoricalNumbers] = useState(true);
  
  // Predictions history for tracking hits
  const [predictionsHistory, setPredictionsHistory] = useState([]);
  const [dozenHits, setDozenHits] = useState(0);
  const [dozenMisses, setDozenMisses] = useState(0);

  const analyzePatternsAndPredict = async (numbers) => {
    if (numbers.length < 5) {
      setError('Se requieren al menos 5 números históricos para el análisis');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Regular pattern analysis
      const response = await axios.post(`${API_BASE_URL}/analyze`, {
        historicalNumbers: numbers
      });

      setPredictions(response.data.topPredictions);
      setStatistics(response.data.statistics);
      setPatternsDetected(response.data.patternsDetected);
      
      // Dozen analysis
      const dozenResponse = await axios.post(`${API_BASE_URL}/analyze-dozens`, {
        historicalNumbers: numbers
      });
      
      setDozenAnalysis(dozenResponse.data);
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

    // Track prediction before adding number
    if (dozenAnalysis && dozenAnalysis.predictions) {
      const dozens = Object.entries(dozenAnalysis.predictions)
        .sort((a, b) => b[1] - a[1]);
      
      const top2Dozens = dozens.slice(0, 2).map(d => d[0]);
      const actualDozen = getDozenName(getDozenFromNumber(num));
      
      const isHit = top2Dozens.includes(actualDozen);
      
      // Update history
      const newPrediction = {
        number: num,
        actualDozen,
        predictedDozens: top2Dozens,
        probabilities: {
          [top2Dozens[0]]: dozens[0][1],
          [top2Dozens[1]]: dozens[1][1]
        },
        isHit,
        timestamp: new Date()
      };
      
      setPredictionsHistory([newPrediction, ...predictionsHistory].slice(0, 20));
      
      if (isHit) {
        setDozenHits(dozenHits + 1);
      } else {
        setDozenMisses(dozenMisses + 1);
      }
    }

    setHistoricalNumbers([...historicalNumbers, num]);
    setInputValue('');
    setError('');
  };

  const getDozenFromNumber = (num) => {
    if (num === 0) return 0;
    if (num >= 1 && num <= 12) return 1;
    if (num >= 13 && num <= 24) return 2;
    if (num >= 25 && num <= 36) return 3;
    return 0;
  };

  const getDozenName = (dozen) => {
    switch(dozen) {
      case 1: return '1st';
      case 2: return '2nd';
      case 3: return '3rd';
      case 0: return 'zero';
      default: return 'unknown';
    }
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
    setDozenAnalysis(null);
    setPredictionsHistory([]);
    setDozenHits(0);
    setDozenMisses(0);
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
                  <button 
                    onClick={() => setShowHistoricalNumbers(!showHistoricalNumbers)}
                    className="btn btn-small"
                  >
                    {showHistoricalNumbers ? 'Contraer' : 'Expandir'}
                  </button>
                  <button onClick={handleRemoveLastNumber} className="btn btn-small">
                    Eliminar Último
                  </button>
                  <button onClick={handleClear} className="btn btn-small btn-danger">
                    Limpiar Todo
                  </button>
                </div>
              </div>
              
              {showHistoricalNumbers && (
                <div className="numbers-grid">
                  {historicalNumbers.map((num, idx) => (
                    <div key={idx} className={`number-chip ${getColorClass(num)}`}>
                      {num}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Dozen Analysis Section */}
        {!loading && dozenAnalysis && (
          <div className="card dozen-analysis-card">
            <div className="dozen-header">
              <h2>🎲 Análisis de Docenas</h2>
              <button 
                onClick={() => setShowDozenView(!showDozenView)}
                className="btn btn-small"
              >
                {showDozenView ? 'Ocultar' : 'Mostrar'}
              </button>
            </div>

            {showDozenView && (
              <>
                {/* Predictions History Stats */}
                {predictionsHistory.length > 0 && (
                  <div className="predictions-stats">
                    <h3>📊 Historial de Aciertos (2 Docenas)</h3>
                    <div className="stats-summary">
                      <div className="stat-box hit">
                        <div className="stat-number">{dozenHits}</div>
                        <div className="stat-label">Aciertos</div>
                      </div>
                      <div className="stat-box miss">
                        <div className="stat-number">{dozenMisses}</div>
                        <div className="stat-label">Fallos</div>
                      </div>
                      <div className="stat-box percentage">
                        <div className="stat-number">
                          {dozenHits + dozenMisses > 0 
                            ? Math.round((dozenHits / (dozenHits + dozenMisses)) * 100)
                            : 0}%
                        </div>
                        <div className="stat-label">Efectividad</div>
                      </div>
                    </div>
                    
                    <div className="recent-predictions">
                      <h4>Últimas 5 predicciones:</h4>
                      {predictionsHistory.slice(0, 5).map((pred, idx) => (
                        <div key={idx} className={`prediction-history-item ${pred.isHit ? 'hit' : 'miss'}`}>
                          <span className={`number-chip small ${getColorClass(pred.number)}`}>
                            {pred.number}
                          </span>
                          <span className="prediction-arrow">→</span>
                          <span className="actual-dozen">{pred.actualDozen}</span>
                          <span className="vs-text">vs</span>
                          <span className="predicted-dozens">
                            {pred.predictedDozens.join(', ')}
                          </span>
                          <span className={`result-badge ${pred.isHit ? 'hit' : 'miss'}`}>
                            {pred.isHit ? '✓ Acertó' : '✗ Falló'}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Probability Bars with Top 2 Highlighting */}
                <div className="dozen-predictions">
                  <h3>🎯 Apuesta en las 2 Docenas con Mayor Probabilidad</h3>
                  
                  {(() => {
                    const sortedDozens = Object.entries(dozenAnalysis.predictions || {})
                      .sort((a, b) => b[1] - a[1]);
                    const top2 = sortedDozens.slice(0, 2).map(d => d[0]);
                    
                    return (
                      <>
                        {/* 1st Dozen */}
                        <div className={`dozen-bar-container ${top2.includes('1st') ? 'recommended' : ''}`}>
                          <div className="dozen-bar-label">
                            <span className="dozen-name">
                              1ª Docena (1-12)
                              {top2.includes('1st') && <span className="recommended-badge">⭐ Apostar</span>}
                            </span>
                            <span className="dozen-percent">
                              {dozenAnalysis.predictions?.['1st']?.toFixed(1) || 0}%
                            </span>
                          </div>
                          <div className="dozen-bar-track">
                            <div 
                              className="dozen-bar-fill first-dozen"
                              style={{
                                width: `${dozenAnalysis.predictions?.['1st'] || 0}%`
                              }}
                            />
                          </div>
                        </div>

                        {/* 2nd Dozen */}
                        <div className={`dozen-bar-container ${top2.includes('2nd') ? 'recommended' : ''}`}>
                          <div className="dozen-bar-label">
                            <span className="dozen-name">
                              2ª Docena (13-24)
                              {top2.includes('2nd') && <span className="recommended-badge">⭐ Apostar</span>}
                            </span>
                            <span className="dozen-percent">
                              {dozenAnalysis.predictions?.['2nd']?.toFixed(1) || 0}%
                            </span>
                          </div>
                          <div className="dozen-bar-track">
                            <div 
                              className="dozen-bar-fill second-dozen"
                              style={{
                                width: `${dozenAnalysis.predictions?.['2nd'] || 0}%`
                              }}
                            />
                          </div>
                        </div>

                        {/* 3rd Dozen */}
                        <div className={`dozen-bar-container ${top2.includes('3rd') ? 'recommended' : ''}`}>
                          <div className="dozen-bar-label">
                            <span className="dozen-name">
                              3ª Docena (25-36)
                              {top2.includes('3rd') && <span className="recommended-badge">⭐ Apostar</span>}
                            </span>
                            <span className="dozen-percent">
                              {dozenAnalysis.predictions?.['3rd']?.toFixed(1) || 0}%
                            </span>
                          </div>
                          <div className="dozen-bar-track">
                            <div 
                              className="dozen-bar-fill third-dozen"
                              style={{
                                width: `${dozenAnalysis.predictions?.['3rd'] || 0}%`
                              }}
                            />
                          </div>
                        </div>

                        <div className="confidence-badge-detailed">
                          <div className="confidence-main">
                            Confianza: <strong>{dozenAnalysis.confidence}</strong>
                          </div>
                          <div className="confidence-explanation">
                            Probabilidad combinada (2 docenas): {' '}
                            <strong>
                              {(sortedDozens[0][1] + sortedDozens[1][1]).toFixed(1)}%
                            </strong>
                          </div>
                        </div>
                      </>
                    );
                  })()}
                </div>

                {/* Strong Patterns */}
                {dozenAnalysis.numberPatterns?.strongPatterns?.length > 0 && (
                  <div className="dozen-patterns">
                    <h3>🔥 Patrones Fuertes Detectados</h3>
                    <div className="pattern-list">
                      {dozenAnalysis.numberPatterns.strongPatterns.slice(0, 5).map((pattern, idx) => (
                        <div key={idx} className="pattern-item-dozen">
                          <span className={`number-chip small ${getColorClass(pattern.number)}`}>
                            {pattern.number}
                          </span>
                          <span className="pattern-arrow">→</span>
                          <span className="pattern-dozen-badge">
                            {pattern.dozen}
                          </span>
                          <span className="pattern-percentage">
                            {pattern.percentage}% ({pattern.occurrences}/{pattern.total})
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Transition Patterns */}
                {dozenAnalysis.transitions?.topTransitions?.length > 0 && (
                  <div className="dozen-transitions">
                    <h3>↔️ Transiciones Más Comunes</h3>
                    <div className="transitions-grid">
                      {dozenAnalysis.transitions.topTransitions.slice(0, 6).map((trans, idx) => (
                        <div key={idx} className="transition-item">
                          <span className="transition-text">{trans[0]}</span>
                          <span className="transition-count">×{trans[1]}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Streak Info */}
                {dozenAnalysis.streaks && (
                  <div className="dozen-streaks">
                    <h3>📈 Rachas Actuales</h3>
                    <p>
                      {dozenAnalysis.streaks.currentDozen === 1 && '1ª Docena'}
                      {dozenAnalysis.streaks.currentDozen === 2 && '2ª Docena'}
                      {dozenAnalysis.streaks.currentDozen === 3 && '3ª Docena'}
                      {dozenAnalysis.streaks.currentDozen === 0 && 'Zero'}
                      {' '}está en racha de <strong>{dozenAnalysis.streaks.currentStreak}</strong> 
                      {dozenAnalysis.streaks.currentStreak === 1 ? ' número' : ' números'}
                    </p>
                    
                    {dozenAnalysis.streaks.last20 && (
                      <div className="hot-cold-analysis">
                        <p><strong>Últimos 20 números:</strong></p>
                        <div className="hot-cold-grid">
                          <div className="hot-cold-item">
                            <span>1ª Docena:</span>
                            <span className="count-badge">{dozenAnalysis.streaks.last20['1st']}</span>
                          </div>
                          <div className="hot-cold-item">
                            <span>2ª Docena:</span>
                            <span className="count-badge">{dozenAnalysis.streaks.last20['2nd']}</span>
                          </div>
                          <div className="hot-cold-item">
                            <span>3ª Docena:</span>
                            <span className="count-badge">{dozenAnalysis.streaks.last20['3rd']}</span>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
        )}

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
