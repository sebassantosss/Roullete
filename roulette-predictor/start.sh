#!/bin/bash

echo "🎰 Roulette Predictor - Quick Start Script"
echo "=========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Node is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 18 or higher."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Backend setup
echo "📦 Setting up backend..."
cd backend

if [ ! -f "target/predictor-backend-1.0.0.jar" ]; then
    echo "Building backend (this may take a few minutes)..."
    mvn clean package -DskipTests
fi

echo "🚀 Starting backend on port 8080..."
java -jar target/predictor-backend-1.0.0.jar &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

cd ..

# Wait for backend to start
echo "⏳ Waiting for backend to be ready..."
sleep 10

# Frontend setup
echo "📦 Setting up frontend..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

echo "🚀 Starting frontend on port 3000..."
npm run dev &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

cd ..

echo ""
echo "✅ Roulette Predictor is running!"
echo ""
echo "🌐 Open your browser at: http://localhost:3000"
echo ""
echo "To stop the application, press Ctrl+C"
echo ""

# Wait for Ctrl+C
wait
