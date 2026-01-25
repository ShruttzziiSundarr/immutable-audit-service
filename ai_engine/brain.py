from flask import Flask, request, jsonify
import networkx as nx
import numpy as np
from sklearn.ensemble import IsolationForest
from geopy.distance import geodesic
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

# --- 1. ADMIN KYC DATABASE (The "World State") ---
# Maps Blockchain IDs to Real Identities & Trust Scores
admin_db = {
    "User_Alice": {"role": "Student", "trust": 100, "home_loc": (12.9716, 77.5946)}, # Bangalore
    "Uber_Driver_Raj": {"role": "Vendor", "trust": 500, "home_loc": (13.0827, 80.2707)}, # Chennai
    "Hacker_007": {"role": "Suspect", "trust": 0, "home_loc": (55.7558, 37.6173)} # Moscow
}

# --- 2. SOCIAL GRAPH (Who trusts whom?) ---
social_graph = nx.Graph()
social_graph.add_edges_from([
    ("User_Alice", "Uber_Driver_Raj"), 
    ("User_Bob", "Uber_Driver_Raj"), 
    ("User_Charlie", "Uber_Driver_Raj"), # Raj is a Hub
    ("User_Dave", "Canteen_Shop")
])

# --- 3. BEHAVIOR MODEL (Anomaly Detection) ---
# Normal spending: [Amount]
normal_data = np.array([[50], [100], [200], [500], [1000], [300]])
model = IsolationForest(contamination=0.1, random_state=42)
model.fit(normal_data)

@app.route('/analyze', methods=['POST'])
def analyze_risk():
    try:
        data = request.json
        sender = data.get('fromAccount')
        receiver = data.get('toAccount')
        amount = float(data.get('amount'))
        current_lat = data.get('lat') # From Frontend GPS
        current_lon = data.get('lon')

        risk_score = 0.0
        reasons = []

        # CHECK 1: ADMIN KYC (Does he exist?)
        receiver_profile = admin_db.get(receiver)
        if not receiver_profile:
            risk_score += 0.4
            reasons.append(f"⚠️ UNKNOWN: {receiver} not in Admin DB.")
        elif receiver_profile['role'] == "Suspect":
            risk_score += 1.0
            reasons.append(f"🚨 BLACKLISTED: {receiver} is a known suspect.")
        
        # CHECK 2: SOCIAL GRAPH (Is he a Hub?)
        connections = social_graph.degree(receiver) if receiver in social_graph else 0
        if connections > 2:
            risk_score -= 0.5
            reasons.append(f"✅ TRUSTED HUB: Connected to {connections} people.")

        # CHECK 3: BEHAVIOR (Is amount weird?)
        if model.predict([[amount]])[0] == -1:
            risk_score += 0.3
            reasons.append(f"⚠️ ANOMALY: ₹{amount} is unusual for you.")

        # CHECK 4: GEOLOCATION (Impossible Travel)
        if current_lat and sender in admin_db:
            home_loc = admin_db[sender]['home_loc']
            distance = geodesic(home_loc, (current_lat, current_lon)).kilometers
            if distance > 500:
                risk_score += 0.9
                reasons.append(f"🚨 IMPOSSIBLE TRAVEL: {int(distance)}km from home!")
            else:
                reasons.append(f"✅ GPS OK: {int(distance)}km from home.")

        # FINAL VERDICT
        decision = "APPROVED"
        if risk_score > 0.6: decision = "BLOCKED"
        elif risk_score > 0.0: decision = "WARNING"

        return jsonify({"decision": decision, "reasons": reasons, "score": risk_score})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    print("🧠 AI BRAIN ACTIVE on Port 5000")
    app.run(port=5000)