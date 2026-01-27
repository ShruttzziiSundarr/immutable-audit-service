<div align="center">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&weight=600&size=30&duration=3000&pause=1000&color=25D366&center=true&vCenter=true&width=600&lines=Shadow+Ledger;AI-Secured+Immutable+Audit;Risk-Adaptive+Cryptography;Merkle+%7C+TSA+%7C+ZKP+%7C+Multi-Sig" alt="Typing SVG" />

  <p align="center">
    <b>An Intelligent Banking Audit System that dynamically switches security protocols based on AI Risk Scoring.</b>
  </p>

<p align="center">
  <a href="https://github.com/ShruttzziiSundarr/immutable-audit-service">
    <img src="https://img.shields.io/github/stars/ShruttzziiSundarr/immutable-audit-service?style=for-the-badge&color=brightgreen" alt="stars" />
  </a>
  <a href="https://github.com/ShruttzziiSundarr/immutable-audit-service/network/members">
    <img src="https://img.shields.io/github/forks/ShruttzziiSundarr/immutable-audit-service?style=for-the-badge&color=orange" alt="forks" />
  </a>
  <a href="https://github.com/ShruttzziiSundarr/immutable-audit-service/issues">
    <img src="https://img.shields.io/github/issues/ShruttzziiSundarr/immutable-audit-service?style=for-the-badge&color=red" alt="issues" />
  </a>
  <a href="https://github.com/ShruttzziiSundarr/immutable-audit-service/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/ShruttzziiSundarr/immutable-audit-service?style=for-the-badge&color=blue" alt="license" />
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Python-3776AB?style=flat&logo=python&logoColor=white" />
  <img src="https://img.shields.io/badge/Flask-000000?style=flat&logo=flask&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-336791?style=flat&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Scikit--Learn-F7931E?style=flat&logo=scikit-learn&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white" />
</p>
</div>

---

## ⚡ See It In Action
> *The system detecting "Impossible Travel" in real-time and switching from Merkle Hashing to Multi-Sig Security.*

![Shadow Ledger Demo](screenshots/demo.gif)
*(Note: Upload a GIF of your project running to a 'screenshots' folder to make this appear!)*

---

## 🚀 The Problem
Traditional banking audit trails are **static**. They apply the same security level to a ₹50 coffee transaction as they do to a ₹50 Lakh corporate transfer.
* **Too Strict:** Wastes computing power on micro-transactions.
* **Too Loose:** Fails to capture legal non-repudiation for high-value transfers.

## 💡 The Solution: "Risk-Adaptive Auditing"
**Shadow Ledger** introduces a **Hybrid Architecture**:
1.  **AI Sentinel (Python):** Analyzes transaction Context (Geolocation, Graph Trust, Anomalies) to generate a Risk Score (0.0 - 1.0).
2.  **Strategy Engine (Kotlin):** Instantly switches the hashing protocol based on the score.

---

## 🛡️ The "Grand Slam" Strategies
The system implements the **Strategy Design Pattern** to hot-swap cryptographic protocols:

| Strategy | Risk Level | Use Case | Technology Implemented |
| :--- | :--- | :--- | :--- |
| **🌳 Merkle Tree** | Low (< 0.2) | Micro-payments | **SHA-256 Batch Hashing** (90% Storage Savings) |
| **⚖️ TSA Legal** | Medium | Asset Transfers | **RSA-2048 Digital Signatures** (RFC 3161 Compliant) |
| **🔐 Multi-Sig** | High (> 0.5) | Corporate | **Dual-Approval** (Admin + System) |
| **👻 ZKP Ghost** | VIP / Privacy | Private Clients | **Hash Commitments** (Zero-Knowledge Proof Simulation) |

---

## 🏗️ Architecture

```mermaid
graph TD
    User[Mobile App] -->|1. Payment Request| API[Audit Controller]
    API -->|2. Context Data| AI[Python Brain]
    AI -- Isolation Forest --> Model[Anomaly Detection]
    AI -- Geopy --> Geo[Impossible Travel Check]
    AI -->|3. Risk Score| API
    API -->|4. Select Strategy| Engine[Strategy Factory]
    Engine -->|Low Risk| Merkle[Merkle Tree]
    Engine -->|High Risk| TSA[RSA Signature]
    Engine -->|Privacy| ZKP[Hash Commitment]
    TSA -->|5. Seal Block| DB[(PostgreSQL Ledger)]
