package com.fin.shadow_ledger.crypto

// A "ProofNode" is one step in the verification path
data class ProofNode(val hash: String, val direction: Direction)
enum class Direction { LEFT, RIGHT }

class MerkleTree(val leafHashes: List<String>) {
    val root: String
    private val tree: List<List<String>> // Stores all levels of the tree

    init {
        // Step 1: Start with the bottom layer (the transactions)
        val levels = mutableListOf(leafHashes)
        
        // Step 2: Keep condensing until we have only 1 hash (the Root)
        while (levels.last().size > 1) {
            levels.add(computeNextLevel(levels.last()))
        }
        
        tree = levels
        root = tree.last().first()
    }

    // Helper: Takes a row of hashes and pairs them up
    private fun computeNextLevel(level: List<String>): List<String> {
        val nextLevel = mutableListOf<String>()
        for (i in level.indices step 2) {
            val left = level[i]
            // If we have an odd number, duplicate the last one to make a pair
            val right = if (i + 1 < level.size) level[i + 1] else left 
            nextLevel.add(HashUtils.combineAndHash(left, right))
        }
        return nextLevel
    }

    // Generates the "Receipt" (Proof) for a specific item
    fun generateProof(index: Int): List<ProofNode> {
        val proof = mutableListOf<ProofNode>()
        var currentIndex = index
        
        // Walk up the tree and grab the "sibling" at each level
        for (i in 0 until tree.size - 1) {
            val level = tree[i]
            val isLeft = currentIndex % 2 == 0
            
            val siblingIndex = if (isLeft) currentIndex + 1 else currentIndex - 1
            
            if (siblingIndex < level.size) {
                proof.add(ProofNode(level[siblingIndex], if (isLeft) Direction.RIGHT else Direction.LEFT))
            } else {
                proof.add(ProofNode(level[currentIndex], Direction.RIGHT))
            }
            currentIndex /= 2 // Move up to the parent
        }
        return proof
    }
}