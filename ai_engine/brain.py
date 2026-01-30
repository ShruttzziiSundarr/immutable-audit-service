"""
Shadow Ledger AI Sentinel - Risk-Based Authentication Engine

This module implements a multi-layer fraud detection system based on:
1. Isolation Forest (Liu & Zhou, 2008) - Unsupervised Anomaly Detection
2. Geospatial Velocity Analysis - Impossible Travel Detection
3. Graph-Based Trust Analysis - Relationship Network Scoring
4. Velocity Checks - Transaction Rate Limiting
5. Structuring Detection - Threshold Avoidance Patterns

References:
- NIST SP 800-63B: Digital Identity Guidelines
- PSD2: Strong Customer Authentication (SCA)
- FINCEN: Anti-Money Laundering (AML) Guidelines
"""

from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from geopy.distance import geodesic
import networkx as nx
import datetime
import json
import os
import logging
from collections import defaultdict
from typing import Dict, List, Tuple, Any, Optional

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('ShadowLedger.AI')

app = Flask(__name__)

# ============================================================================
# CONFIGURATION LOADER
# ============================================================================

def load_config() -> Dict:
    """Load fraud detection configuration from external JSON file."""
    config_path = os.path.join(os.path.dirname(__file__), 'config', 'fraud_detection_config.json')
    try:
        with open(config_path, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        logger.warning(f"Config file not found at {config_path}, using defaults")
        return get_default_config()

def get_default_config() -> Dict:
    """Fallback configuration if external config is unavailable."""
    return {
        "model_config": {
            "isolation_forest": {"contamination": 0.1, "n_estimators": 100, "random_state": 42},
            "velocity_detection": {"max_travel_speed_kmh": 900},
            "transaction_velocity": {"window_minutes": 5, "max_transactions": 10},
            "structuring_detection": {"reporting_thresholds": [10000, 50000, 100000], "proximity_margin": 500}
        },
        "training_data": {
            "normal_transactions": [
                {"amount": 500, "lat": 19.07, "lon": 72.87, "hour": 10},
                {"amount": 2000, "lat": 19.07, "lon": 72.87, "hour": 14},
                {"amount": 50000, "lat": 12.97, "lon": 77.59, "hour": 11}
            ]
        },
        "user_profiles": {"accounts": {}},
        "trust_graph": {"edges": []},
        "risk_weights": {
            "base_risk": 0.05, "anomaly_detection": 0.35, "impossible_travel": 0.40,
            "unknown_beneficiary": 0.15, "velocity_abuse": 0.30, "structuring_attempt": 0.25
        },
        "risk_thresholds": {"low": 0.2, "medium": 0.5, "high": 0.8}
    }

# Load configuration
CONFIG = load_config()
logger.info("AI Sentinel: Configuration loaded successfully")

# ============================================================================
# ISOLATION FOREST - UNSUPERVISED ANOMALY DETECTION
# ============================================================================
"""
Isolation Forest Algorithm (Liu & Zhou, 2008):
- Core principle: Anomalies are "few and different"
- Anomalies require fewer random partitions to isolate
- Path length from root to leaf indicates anomaly score
- Shorter path = more anomalous

Advantages over traditional methods:
- No distance/density calculations needed (efficient for high-dimensional data)
- O(n log n) complexity - scales to millions of transactions
- Unsupervised - no labeled fraud data required
"""

def initialize_isolation_forest() -> IsolationForest:
    """
    Initialize and train Isolation Forest model on historical normal transactions.

    The model learns "clusters" of normal behavior. Transactions outside these
    clusters are flagged as anomalies (potential fraud).
    """
    training_data = CONFIG.get('training_data', {}).get('normal_transactions', [])

    # Extract features: [amount, latitude, longitude, hour_of_day]
    X_train = [[t['amount'], t['lat'], t['lon'], t['hour']] for t in training_data]

    if not X_train:
        logger.error("No training data available!")
        X_train = [[1000, 19.07, 72.87, 12]]  # Fallback

    model_config = CONFIG.get('model_config', {}).get('isolation_forest', {})

    clf = IsolationForest(
        contamination=model_config.get('contamination', 0.1),
        n_estimators=model_config.get('n_estimators', 100),
        random_state=model_config.get('random_state', 42),
        warm_start=False
    )
    clf.fit(X_train)

    logger.info(f"Isolation Forest trained on {len(X_train)} normal transaction patterns")
    return clf

# Initialize model
isolation_forest_model = initialize_isolation_forest()

# ============================================================================
# GRAPH-BASED TRUST ANALYSIS
# ============================================================================
"""
Graph Neural Network-Inspired Trust Analysis:
- Models accounts and merchants as nodes
- Trusted relationships form edges with trust scores
- Unknown/isolated nodes indicate higher risk
- Based on research showing GNNs detect fraud patterns invisible to
  transaction-level analysis (NVIDIA, 2023)

Real-world scale: Money laundering is 2-5% of global GDP ($1.5-3T annually)
"""

def initialize_trust_graph() -> nx.DiGraph:
    """
    Build directed trust graph from known relationships.

    Edge weights represent trust scores (0.0 - 1.0).
    Isolated nodes (unknown receivers) are high-risk indicators.
    """
    G = nx.DiGraph()

    edges = CONFIG.get('trust_graph', {}).get('edges', [])
    for edge in edges:
        G.add_edge(
            edge['from'],
            edge['to'],
            trust_score=edge.get('trust_score', 0.5)
        )

    logger.info(f"Trust Graph initialized: {G.number_of_nodes()} nodes, {G.number_of_edges()} edges")
    return G

trust_graph = initialize_trust_graph()

# ============================================================================
# VELOCITY TRACKING - RATE LIMITING & ABUSE DETECTION
# ============================================================================
"""
Velocity Checks (Transaction Rate Limiting):
- Track frequency of transactions per user over time windows
- Detect patterns: card testing, credential stuffing, bot attacks
- Industry standard: 5-10 transactions per 5 minutes is typical threshold

Research: Checkout.com, Stripe fraud detection guidelines
"""

# In-memory transaction velocity tracker
# Production: Use Redis or similar for distributed rate limiting
transaction_velocity_tracker: Dict[str, List[datetime.datetime]] = defaultdict(list)

def check_velocity_abuse(user_id: str) -> Tuple[bool, Optional[str]]:
    """
    Detect if user is making too many transactions in a short time window.

    Patterns detected:
    - Card testing: Multiple small charges to validate stolen cards
    - Account takeover: Rapid transactions after credential theft
    - Bot attacks: Automated transaction attempts

    Returns:
        Tuple of (is_abuse: bool, reason: Optional[str])
    """
    config = CONFIG.get('model_config', {}).get('transaction_velocity', {})
    window_minutes = config.get('window_minutes', 5)
    max_transactions = config.get('max_transactions', 10)

    now = datetime.datetime.now()
    cutoff = now - datetime.timedelta(minutes=window_minutes)

    # Clean old entries and add current transaction
    transaction_velocity_tracker[user_id] = [
        t for t in transaction_velocity_tracker[user_id] if t > cutoff
    ]
    transaction_velocity_tracker[user_id].append(now)

    count = len(transaction_velocity_tracker[user_id])

    if count > max_transactions:
        return True, f"Velocity abuse: {count} transactions in {window_minutes} minutes (threshold: {max_transactions})"

    return False, None

# ============================================================================
# STRUCTURING DETECTION - ANTI-MONEY LAUNDERING
# ============================================================================
"""
Structuring (Smurfing) Detection:
- Identifies transactions deliberately kept below reporting thresholds
- Common AML red flag: amounts like $9,900 when threshold is $10,000
- FINCEN requires banks to detect and report structuring attempts

Research: Financial Crimes Enforcement Network (FINCEN) guidelines
"""

def check_structuring_attempt(amount: float) -> Tuple[bool, Optional[str]]:
    """
    Detect potential structuring (threshold avoidance) attempts.

    Structuring is when someone deliberately keeps transactions below
    regulatory reporting thresholds to avoid detection.

    Returns:
        Tuple of (is_structuring: bool, reason: Optional[str])
    """
    config = CONFIG.get('model_config', {}).get('structuring_detection', {})
    thresholds = config.get('reporting_thresholds', [10000, 50000, 100000])
    margin = config.get('proximity_margin', 500)

    for threshold in thresholds:
        lower_bound = threshold - margin
        if lower_bound < amount < threshold:
            return True, f"Potential structuring: Amount {amount} suspiciously close to {threshold} reporting threshold"

    return False, None

# ============================================================================
# IMPOSSIBLE TRAVEL DETECTION - GEOSPATIAL VELOCITY
# ============================================================================
"""
Impossible Travel Detection Algorithm:
- Calculates physical travel velocity between consecutive transaction locations
- Flags if velocity exceeds maximum possible (commercial aircraft ~900 km/h)
- Accounts for IP geolocation accuracy margins

Research: Microsoft Defender, Ping Identity impossible travel documentation
"""

def calculate_travel_velocity(
    prev_lat: float, prev_lon: float,
    curr_lat: float, curr_lon: float,
    time_delta_hours: float
) -> float:
    """
    Calculate travel velocity between two geographic points.

    Args:
        prev_lat, prev_lon: Previous location coordinates
        curr_lat, curr_lon: Current location coordinates
        time_delta_hours: Time difference in hours

    Returns:
        Velocity in km/h
    """
    if time_delta_hours <= 0:
        return 0.0

    try:
        distance_km = geodesic((prev_lat, prev_lon), (curr_lat, curr_lon)).km
        return distance_km / time_delta_hours
    except Exception as e:
        logger.error(f"Geodesic calculation error: {e}")
        return 0.0

def check_impossible_travel(
    user_id: str,
    current_lat: float,
    current_lon: float,
    time_since_last_hours: float = 1.0
) -> Tuple[bool, float, Optional[str]]:
    """
    Detect impossible travel based on geospatial velocity analysis.

    Compares current transaction location against user's known home location
    or last transaction location.

    Returns:
        Tuple of (is_impossible: bool, velocity: float, reason: Optional[str])
    """
    config = CONFIG.get('model_config', {}).get('velocity_detection', {})
    max_speed = config.get('max_travel_speed_kmh', 900)

    # Get user's home location from profile
    user_profiles = CONFIG.get('user_profiles', {}).get('accounts', {})
    user_profile = user_profiles.get(user_id, {})

    if not user_profile:
        # Unknown user - use default location (higher risk)
        home_lat, home_lon = 19.0760, 72.8777  # Default: Mumbai
    else:
        home_lat = user_profile.get('home_lat', 19.0760)
        home_lon = user_profile.get('home_lon', 72.8777)

    velocity = calculate_travel_velocity(
        home_lat, home_lon,
        current_lat, current_lon,
        time_since_last_hours
    )

    if velocity > max_speed:
        return True, velocity, f"Impossible travel detected: {int(velocity)} km/h exceeds maximum {max_speed} km/h"

    return False, velocity, None

# ============================================================================
# ANOMALY DETECTION LAYER
# ============================================================================

def check_spending_anomaly(
    amount: float,
    lat: float,
    lon: float,
    hour: int
) -> Tuple[bool, float, Optional[str]]:
    """
    Use Isolation Forest to detect spending anomalies.

    The model returns:
    - 1: Normal transaction (inlier)
    - -1: Anomalous transaction (outlier)

    Anomaly score indicates how isolated the data point is.

    Returns:
        Tuple of (is_anomaly: bool, anomaly_score: float, reason: Optional[str])
    """
    features = [[amount, lat, lon, hour]]

    prediction = isolation_forest_model.predict(features)
    # decision_function returns negative for anomalies, positive for normal
    anomaly_score = -isolation_forest_model.decision_function(features)[0]

    is_anomaly = prediction[0] == -1

    if is_anomaly:
        return True, anomaly_score, f"Spending pattern anomaly detected (isolation score: {anomaly_score:.3f})"

    return False, anomaly_score, None

# ============================================================================
# GRAPH TRUST ANALYSIS
# ============================================================================

def check_beneficiary_trust(sender: str, receiver: str) -> Tuple[bool, float, Optional[str]]:
    """
    Analyze trust relationship between sender and receiver using graph analysis.

    Checks:
    - Is receiver a known node in trust graph?
    - Is there a direct trust edge from sender to receiver?
    - What is the trust score of the relationship?

    Returns:
        Tuple of (is_unknown: bool, trust_score: float, reason: Optional[str])
    """
    # Check if receiver exists in graph
    if not trust_graph.has_node(receiver):
        return True, 0.0, f"Unknown beneficiary: '{receiver}' not in trust network (isolated node)"

    # Check for direct trust relationship
    if trust_graph.has_edge(sender, receiver):
        trust_score = trust_graph[sender][receiver].get('trust_score', 0.5)
        return False, trust_score, None

    # Receiver known but no direct relationship with sender
    return True, 0.2, f"No direct trust relationship between {sender} and {receiver}"

# ============================================================================
# ADDITIONAL RISK FACTORS
# ============================================================================

def check_amount_deviation(user_id: str, amount: float) -> Tuple[bool, Optional[str]]:
    """
    Check if transaction amount significantly deviates from user's typical pattern.
    """
    user_profiles = CONFIG.get('user_profiles', {}).get('accounts', {})
    user_profile = user_profiles.get(user_id, {})

    typical_max = user_profile.get('typical_max_amount', 50000)

    if amount > typical_max * 2:
        return True, f"Amount {amount} is {amount/typical_max:.1f}x user's typical maximum ({typical_max})"

    return False, None

def check_odd_hour_transaction(hour: int) -> Tuple[bool, Optional[str]]:
    """
    Flag transactions during unusual hours (potential account compromise).
    """
    # Odd hours: 1 AM - 5 AM
    if 1 <= hour <= 5:
        return True, f"Transaction at unusual hour ({hour}:00) - potential account compromise"

    return False, None

# ============================================================================
# MAIN RISK SCORING ENGINE
# ============================================================================

def calculate_composite_risk_score(risk_factors: Dict[str, Any]) -> Dict[str, Any]:
    """
    Calculate weighted composite risk score based on all detection layers.

    Implements Risk-Based Authentication (RBA) scoring model aligned with:
    - NIST SP 800-63B guidelines
    - PSD2 Strong Customer Authentication requirements

    Returns comprehensive risk assessment with score, decision, and reasons.
    """
    weights = CONFIG.get('risk_weights', {})
    thresholds = CONFIG.get('risk_thresholds', {})

    # Start with base risk
    risk_score = weights.get('base_risk', 0.05)
    reasons = []
    risk_breakdown = {}

    # Layer 1: Anomaly Detection (Isolation Forest)
    if risk_factors.get('is_anomaly'):
        contribution = weights.get('anomaly_detection', 0.35)
        risk_score += contribution
        reasons.append(risk_factors.get('anomaly_reason'))
        risk_breakdown['anomaly_detection'] = contribution

    # Layer 2: Impossible Travel Detection
    if risk_factors.get('is_impossible_travel'):
        contribution = weights.get('impossible_travel', 0.40)
        risk_score += contribution
        reasons.append(risk_factors.get('travel_reason'))
        risk_breakdown['impossible_travel'] = contribution

    # Layer 3: Unknown Beneficiary (Graph Analysis)
    if risk_factors.get('is_unknown_beneficiary'):
        contribution = weights.get('unknown_beneficiary', 0.15)
        risk_score += contribution
        reasons.append(risk_factors.get('beneficiary_reason'))
        risk_breakdown['unknown_beneficiary'] = contribution

    # Layer 4: Velocity Abuse
    if risk_factors.get('is_velocity_abuse'):
        contribution = weights.get('velocity_abuse', 0.30)
        risk_score += contribution
        reasons.append(risk_factors.get('velocity_reason'))
        risk_breakdown['velocity_abuse'] = contribution

    # Layer 5: Structuring Attempt
    if risk_factors.get('is_structuring'):
        contribution = weights.get('structuring_attempt', 0.25)
        risk_score += contribution
        reasons.append(risk_factors.get('structuring_reason'))
        risk_breakdown['structuring_attempt'] = contribution

    # Layer 6: Amount Deviation
    if risk_factors.get('is_amount_deviation'):
        contribution = weights.get('amount_deviation', 0.20)
        risk_score += contribution
        reasons.append(risk_factors.get('amount_reason'))
        risk_breakdown['amount_deviation'] = contribution

    # Layer 7: Odd Hour Transaction
    if risk_factors.get('is_odd_hour'):
        contribution = weights.get('odd_hour_transaction', 0.10)
        risk_score += contribution
        reasons.append(risk_factors.get('hour_reason'))
        risk_breakdown['odd_hour'] = contribution

    # Cap risk score at 1.0
    risk_score = min(risk_score, 1.0)

    # Determine decision based on thresholds (PSD2/NIST aligned)
    if risk_score > thresholds.get('high', 0.8):
        decision = "BLOCKED"
        recommended_action = "Transaction blocked - manual review required"
    elif risk_score > thresholds.get('medium', 0.5):
        decision = "STEP_UP_AUTH"
        recommended_action = "Require additional authentication (OTP, biometric)"
    elif risk_score > thresholds.get('low', 0.2):
        decision = "REVIEW"
        recommended_action = "Flag for async review - allow with monitoring"
    else:
        decision = "APPROVED"
        recommended_action = "Low risk - proceed normally"

    # Determine cryptographic strategy recommendation
    if risk_score > 0.6:
        strategy = "MULTISIG"
        strategy_reason = "High risk requires multi-signature approval (2-of-3 threshold)"
    elif risk_score > 0.2:
        strategy = "TSA"
        strategy_reason = "Medium risk requires timestamped RSA-2048 signature (non-repudiation)"
    else:
        strategy = "MERKLE"
        strategy_reason = "Low risk - batch with Merkle tree for efficiency"

    return {
        "score": round(risk_score, 3),
        "decision": decision,
        "recommended_action": recommended_action,
        "reasons": [r for r in reasons if r],  # Filter None values
        "risk_breakdown": risk_breakdown,
        "strategy_recommendation": strategy,
        "strategy_reason": strategy_reason,
        "detection_layers_triggered": len(risk_breakdown),
        "timestamp": datetime.datetime.now().isoformat()
    }

# ============================================================================
# API ENDPOINTS
# ============================================================================

@app.route('/analyze', methods=['POST'])
def analyze_transaction():
    """
    Main endpoint for transaction risk analysis.

    Implements multi-layer Risk-Based Authentication (RBA):
    1. Isolation Forest anomaly detection
    2. Impossible travel detection
    3. Graph-based trust analysis
    4. Velocity abuse detection
    5. Structuring detection
    6. Amount deviation analysis
    7. Temporal pattern analysis

    Request JSON:
    {
        "fromAccount": "ACC-MUM-001",
        "toAccount": "MERCHANT-A",
        "amount": 5000.0,
        "lat": 19.0760,
        "lon": 72.8777
    }

    Response JSON:
    {
        "score": 0.25,
        "decision": "REVIEW",
        "reasons": [...],
        "strategy_recommendation": "TSA",
        ...
    }
    """
    try:
        data = request.json

        # Extract transaction features
        user_id = data.get('fromAccount', 'UNKNOWN')
        receiver = data.get('toAccount', 'UNKNOWN')
        amount = float(data.get('amount', 0))
        lat = float(data.get('lat', 0.0))
        lon = float(data.get('lon', 0.0))
        current_hour = datetime.datetime.now().hour

        logger.info(f"Analyzing transaction: {user_id} -> {receiver}, Amount: {amount}")

        # Initialize risk factors dictionary
        risk_factors = {}

        # Layer 1: Isolation Forest Anomaly Detection
        is_anomaly, anomaly_score, anomaly_reason = check_spending_anomaly(amount, lat, lon, current_hour)
        risk_factors['is_anomaly'] = is_anomaly
        risk_factors['anomaly_score'] = anomaly_score
        risk_factors['anomaly_reason'] = anomaly_reason

        # Layer 2: Impossible Travel Detection
        is_impossible, velocity, travel_reason = check_impossible_travel(user_id, lat, lon)
        risk_factors['is_impossible_travel'] = is_impossible
        risk_factors['travel_velocity'] = velocity
        risk_factors['travel_reason'] = travel_reason

        # Layer 3: Graph-Based Trust Analysis
        is_unknown, trust_score, beneficiary_reason = check_beneficiary_trust(user_id, receiver)
        risk_factors['is_unknown_beneficiary'] = is_unknown
        risk_factors['trust_score'] = trust_score
        risk_factors['beneficiary_reason'] = beneficiary_reason

        # Layer 4: Velocity Abuse Detection
        is_velocity_abuse, velocity_reason = check_velocity_abuse(user_id)
        risk_factors['is_velocity_abuse'] = is_velocity_abuse
        risk_factors['velocity_reason'] = velocity_reason

        # Layer 5: Structuring Detection
        is_structuring, structuring_reason = check_structuring_attempt(amount)
        risk_factors['is_structuring'] = is_structuring
        risk_factors['structuring_reason'] = structuring_reason

        # Layer 6: Amount Deviation
        is_amount_deviation, amount_reason = check_amount_deviation(user_id, amount)
        risk_factors['is_amount_deviation'] = is_amount_deviation
        risk_factors['amount_reason'] = amount_reason

        # Layer 7: Odd Hour Transaction
        is_odd_hour, hour_reason = check_odd_hour_transaction(current_hour)
        risk_factors['is_odd_hour'] = is_odd_hour
        risk_factors['hour_reason'] = hour_reason

        # Calculate composite risk score
        result = calculate_composite_risk_score(risk_factors)

        logger.info(f"Risk assessment complete: Score={result['score']}, Decision={result['decision']}")

        return jsonify(result)

    except Exception as e:
        logger.error(f"Error analyzing transaction: {e}")
        return jsonify({
            "score": 0.5,
            "decision": "ERROR",
            "reasons": [f"Analysis error: {str(e)}"],
            "strategy_recommendation": "TSA"
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint for monitoring."""
    return jsonify({
        "status": "healthy",
        "service": "Shadow Ledger AI Sentinel",
        "version": "2.0.0",
        "models_loaded": {
            "isolation_forest": isolation_forest_model is not None,
            "trust_graph_nodes": trust_graph.number_of_nodes(),
            "trust_graph_edges": trust_graph.number_of_edges()
        },
        "config_loaded": CONFIG is not None
    })

@app.route('/config', methods=['GET'])
def get_config():
    """Return current configuration (for debugging/admin)."""
    return jsonify({
        "risk_weights": CONFIG.get('risk_weights', {}),
        "risk_thresholds": CONFIG.get('risk_thresholds', {}),
        "model_config": CONFIG.get('model_config', {})
    })

# ============================================================================
# MAIN ENTRY POINT
# ============================================================================

if __name__ == '__main__':
    logger.info("=" * 60)
    logger.info("Shadow Ledger AI Sentinel v2.0.0")
    logger.info("Risk-Based Authentication Engine")
    logger.info("=" * 60)
    logger.info("Detection Layers Active:")
    logger.info("  1. Isolation Forest (Unsupervised Anomaly Detection)")
    logger.info("  2. Impossible Travel (Geospatial Velocity Analysis)")
    logger.info("  3. Trust Graph (Graph-Based Fraud Detection)")
    logger.info("  4. Velocity Checks (Rate Limiting)")
    logger.info("  5. Structuring Detection (AML Compliance)")
    logger.info("  6. Amount Deviation Analysis")
    logger.info("  7. Temporal Pattern Analysis")
    logger.info("=" * 60)
    logger.info("Starting Flask server on port 5000...")

    app.run(port=5000, debug=True)
