from flask import Flask, request, jsonify
import logging
import datetime

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger('ShadowLedger.AI')

app = Flask(__name__)

@app.route('/analyze', methods=['POST'])
def analyze_transaction():
    """
    FORCED DEMO MODE:
    - Amounts < 1000 are ALWAYS Safe (Green)
    - Amounts >= 5000 are ALWAYS Blocked (Red)
    """
    try:
        data = request.json
        amount = float(data.get('amount', 0))
        user_id = data.get('fromAccount', 'UNKNOWN')

        logger.info(f"🧠 AI ANALYZING: {user_id} sending ₹{amount}")

        # --- THE HARDCODED DEMO LOGIC ---
        
        if amount < 1000:
            # SCENARIO 1: SAFE (Green)
            logger.info("✅ DECISION: SAFE (Demo Override)")
            return jsonify({
                "score": 0.05,
                "decision": "APPROVED",
                "reasons": ["Identity Verified", "Location Consistent"],
                "strategy_recommendation": "TSA",
                "strategy_reason": "Low Risk Verified"
            })

        else:
            # SCENARIO 2: RISKY (Red)
            logger.info("❌ DECISION: BLOCKED (Demo Override)")
            return jsonify({
                "score": 0.95,
                "decision": "BLOCKED",
                "reasons": ["ANOMALY_DETECTED", "HIGH_VALUE", "VELOCITY_SPIKE"],
                "strategy_recommendation": "MULTISIG",
                "strategy_reason": "Risk Threshold Exceeded"
            })

    except Exception as e:
        logger.error(f"Error: {e}")
        return jsonify({"score": 0.1, "decision": "APPROVED"}), 200

if __name__ == '__main__':
    logger.info("🚀 AI BRAIN STARTED (DEMO MODE)")
    app.run(port=5000)