from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from geopy.distance import geodesic
import networkx as nx
import datetime

app = Flask(__name__)

# --- 1. MEMORY & TRAINING ---
# In a real app, this loads from a database. Here, we train on startup.
print(" AI ENGINE: Initializing & Training Models...")

# Mock "History" Data (User's normal behavior)
# [Amount, Lat, Lon, Hour_of_Day]
training_data = [
    [500, 19.07, 72.87, 10],   # Coffee in Mumbai (Morning)
    [2000, 19.07, 72.87, 14],  # Lunch in Mumbai
    [150, 19.08, 72.88, 18],   # Snacks in Mumbai
    [50000, 12.97, 77.59, 11], # Big txn in Bangalore (Valid VIP)
]

# Train Isolation Forest (Unsupervised Anomaly Detection)
# It learns "Clusters" of normal behavior. Anything outside is an anomaly.
clf = IsolationForest(contamination=0.1, random_state=42)
clf.fit(training_data)

# Trust Graph (NetworkX)
# We map known "Good" accounts. Isolated nodes are suspicious.
G = nx.Graph()
G.add_edge("ACC-MUM-001", "MERCHANT-A") # Rahul trusts Merchant A
G.add_edge("ACC-BLR-VIP", "MERCHANT-B") # Priya trusts Merchant B

print("✅ AI ENGINE: Online & Listening on Port 5000")

# --- 2. HELPER FUNCTIONS ---
def calculate_velocity(prev_lat, prev_lon, curr_lat, curr_lon, time_delta_hours):
    """Calculates speed of travel (km/h). If > 900 km/h, it's impossible."""
    if prev_lat is None or time_delta_hours == 0:
        return 0.0
    
    coords_1 = (prev_lat, prev_lon)
    coords_2 = (curr_lat, curr_lon)
    distance = geodesic(coords_1, coords_2).km
    return distance / time_delta_hours

# --- 3. THE ANALYZE ENDPOINT ---
@app.route('/analyze', methods=['POST'])
def analyze_transaction():
    data = request.json
    
    # Extract Features
    user_id = data.get('fromAccount')
    amount = data.get('amount')
    lat = data.get('lat', 0.0)
    lon = data.get('lon', 0.0)
    
    # --- LOGIC LAYER 1: ANOMALY DETECTION (ML) ---
    # We ask the model: "Is this transaction normal compared to history?"
    # 1 = Normal, -1 = Anomaly
    # We assume 'Hour 12' as a dummy time feature for now
    prediction = clf.predict([[amount, lat, lon, 12]]) 
    is_anomaly = prediction[0] == -1

    # --- LOGIC LAYER 2: IMPOSSIBLE TRAVEL (PHYSICS) ---
    # (Simplified: We assume the user was at 'Home' 1 hour ago)
    # In prod, we fetch the *actual* last transaction time/location.
    home_lat = 19.0760 # Hardcoded for demo (Mumbai)
    velocity = calculate_velocity(home_lat, 72.8777, lat, lon, 1.0) # 1 hour diff
    
    impossible_travel = velocity > 800 # Faster than a plane?

    # --- LOGIC LAYER 3: GRAPH TRUST ---
    # Is the receiver unknown?
    receiver = data.get('toAccount')
    known_receiver = G.has_node(receiver)

    # --- SCORING ENGINE ---
    risk_score = 0.1 # Base Risk
    reasons = []

    if is_anomaly:
        risk_score += 0.4
        reasons.append(f"Spending Anomaly (Amount: {amount})")

    if impossible_travel:
        risk_score += 0.5
        reasons.append(f"Impossible Travel (Speed: {int(velocity)} km/h)")

    if not known_receiver:
        risk_score += 0.2
        reasons.append("Unknown Beneficiary (Graph Isolation)")

    # Cap score at 1.0
    risk_score = min(risk_score, 1.0)

    # --- DECISION ---
    decision = "APPROVED"
    if risk_score > 0.8:
        decision = "BLOCKED"
    elif risk_score > 0.5:
        decision = "REVIEW"

    return jsonify({
        "score": round(risk_score, 2),
        "decision": decision,
        "reasons": reasons,
        "strategy_recommendation": "MULTISIG" if risk_score > 0.6 else ("TSA" if risk_score > 0.2 else "MERKLE")
    })

if __name__ == '__main__':
    app.run(port=5000, debug=True)