package online.mempool.fatline.data

/** Time in seconds since farcaster epoch */
fun farcasterEpochNow(): Int = ((System.currentTimeMillis() / 1000) - 1609459200).toInt()