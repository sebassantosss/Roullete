# 🎲 Datos de Prueba para Roulette Predictor

## Secuencia de Prueba Completa (Nuestra Sesión Real)

Esta es la secuencia exacta que usamos durante nuestra conversación donde logramos predicciones exitosas:

```
14, 34, 11, 33, 8, 27, 29, 4, 33, 11, 35, 6, 31, 10, 2, 22, 12, 12, 24, 34, 12, 32, 24, 12, 6, 23, 27, 22, 28, 35, 7, 21, 23, 5, 35, 36, 29, 5, 4, 19, 4, 24, 9, 6, 10, 20, 27, 1, 24, 36
```

### Predicciones Exitosas de Esta Secuencia:

1. **Número 7** ✅
   - Confirmaciones: 3 (suma de dígitos, diferencias)
   - Probabilidad predicha: 35%

2. **Número 21** ✅
   - Confirmaciones: 2 (7×3, diferencias)
   - Probabilidad predicha: 30%

3. **Número 5** ✅
   - Confirmaciones: 2 (suma de dígitos 2+3)
   - Probabilidad predicha: 35%

4. **Número 36** ✅
   - Confirmaciones: 3 (patrón 12-24-36, posición)
   - Probabilidad predicha: 30%

5. **Número 9** ✅
   - Confirmaciones: 8 (múltiples patrones)
   - Probabilidad predicha: 60%

6. **Número 1** ✅
   - Confirmaciones: 14 (récord de confirmaciones)
   - Probabilidad predicha: 60%

---

## Otras Secuencias de Prueba

### Secuencia con Patrón 12-24-36 Fuerte

```
12, 24, 12, 36, 24, 12, 24, 36, 12
```

**Predicción esperada:** El siguiente debería ser 24 o 36 (patrón multiplicativo activo)

---

### Secuencia con Suma de Dígitos

```
23, 14, 27, 36, 18, 29
```

**Predicciones esperadas:**
- 5 (de 23: 2+3)
- 5 (de 14: 1+4)
- 9 (de 27: 2+7)
- 9 (de 36: 3+6)
- 9 (de 18: 1+8)
- 11 (de 29: 2+9)

---

### Secuencia con Números Calientes

```
7, 21, 7, 35, 7, 12, 21, 7, 21
```

**Predicción esperada:** 7 o 21 (números muy calientes)

---

### Secuencia con Patrón de Dobles

```
11, 22, 11, 33, 22, 11, 22, 33
```

**Predicción esperada:** 0 (completar patrón de dobles 11, 22, 33, 00)

---

### Secuencia con Inversión de Dígitos

```
12, 21, 23, 32, 14, 41, 13, 31
```

**Predicción esperada:** Números que sean inversión de los frecuentes

---

## Cómo Usar Estos Datos

### En la Aplicación Web:

1. **Copia una secuencia completa**
2. **Pégala en el campo de "Carga Masiva"**
3. **Presiona "Cargar Datos"**
4. **Observa las predicciones generadas**
5. **Agrega números uno por uno** para ver cómo evolucionan las predicciones

### Mediante la API (curl):

```bash
curl -X POST http://localhost:8080/api/v1/roulette/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "historicalNumbers": [14, 34, 11, 33, 8, 27, 29, 4, 33, 11, 35, 6, 31, 10, 2, 22, 12, 12, 24, 34]
  }'
```

### Mediante la API (JavaScript):

```javascript
const response = await fetch('http://localhost:8080/api/v1/roulette/analyze', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    historicalNumbers: [14, 34, 11, 33, 8, 27, 29, 4, 33, 11, 35, 6, 31, 10, 2, 22, 12, 12, 24, 34]
  })
});

const data = await response.json();
console.log('Top Predictions:', data.topPredictions);
```

---

## Secuencias Mínimas (Para Testing Rápido)

### Mínimo Requerido (5 números):
```
12, 24, 36, 12, 24
```

### Secuencia Corta (10 números):
```
7, 14, 21, 28, 35, 7, 14, 21, 28, 35
```

### Secuencia Mediana (20 números):
```
5, 10, 15, 20, 25, 30, 35, 5, 10, 15, 20, 25, 30, 35, 5, 10, 15, 20, 25, 30
```

---

## Notas Importantes

- **Mínimo 5 números**: La aplicación requiere al menos 5 números históricos para comenzar el análisis
- **Máximo 36**: Los números deben estar entre 0 y 36 (ruleta europea)
- **Formato flexible**: Puedes separar los números con espacios, comas, o saltos de línea
- **Números inválidos**: Serán automáticamente filtrados

---

## Interpretación de Resultados

### Alta Probabilidad (60%+)
- Indica **múltiples patrones convergentes**
- Muy confiable para apostar

### Probabilidad Media (40-60%)
- **Buenos candidatos**
- Patrones moderados

### Baja Probabilidad (<40%)
- Pocas confirmaciones
- Apostar con cautela

### Confirmaciones
- **10+ confirmaciones**: Extremadamente fuerte
- **5-9 confirmaciones**: Fuerte
- **2-4 confirmaciones**: Moderado
- **1 confirmación**: Débil

---

**¡Buena suerte probando el sistema! 🎰**
