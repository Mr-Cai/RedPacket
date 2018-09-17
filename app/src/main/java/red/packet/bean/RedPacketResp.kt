package red.packet.bean

import java.io.Serializable

/**
 * Description:
 */

class RedPacketResp : Serializable {

    var isWithAnimation: Boolean = false
    var redPocketAmount: Double = 0.toDouble()
    var account: String? = null
}
