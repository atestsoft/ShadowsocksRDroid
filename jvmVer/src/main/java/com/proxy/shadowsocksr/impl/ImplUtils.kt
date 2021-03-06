package com.proxy.shadowsocksr.impl

import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.util.zip.CRC32

object ImplUtils
{
    private val md5: MessageDigest = MessageDigest.getInstance(
            "MD5")// I think all api14+ devices have md5

    fun fillCRC32(src: ByteArray, dst: ByteArray, dstOff: Int)
    {
        val crc32 = CRC32()
        crc32.update(src)
        val value = crc32.value.inv()
        dst[dstOff] = value.toByte()
        dst[dstOff + 1] = (value shr 8).toByte()
        dst[dstOff + 2] = (value shr 16).toByte()
        dst[dstOff + 3] = (value shr 24).toByte()
    }

    fun getCRC32(src: ByteArray): ByteArray
    {
        val out = ByteArray(4)
        fillCRC32(src, out, 0)
        return out
    }

    private val srnd: SecureRandom = SecureRandom()
    private val rnd: Random = Random()

    fun randomInt(up: Int): Int = rnd.nextInt(up)

    fun randomBytes(cnt: Int): ByteArray
    {
        val out = ByteArray(cnt)
        rnd.nextBytes(out)
        return out
    }

    fun srandomBytes(cnt: Int): ByteArray
    {
        val out = ByteArray(cnt)
        srnd.nextBytes(out)
        return out
    }

    fun fillEpoch(dst: ByteArray, dstOff: Int)
    {
        val cur = (System.currentTimeMillis() / 1000L)//not use high 32bit,need not mask it.
        dst[dstOff] = cur.toByte()
        dst[dstOff + 1] = (cur shr 8).toByte()
        dst[dstOff + 2] = (cur shr 16).toByte()
        dst[dstOff + 3] = (cur shr 24).toByte()
    }

    fun findRightHeadSize(buf: ByteArray, dft: Int): Int
    {
        if (buf.size < 2)
        {
            return dft
        }
        when (buf[0].toInt() and 0xFF)
        {
            1    -> return 7 //ipv4
            3    -> return 4 + (buf[1].toInt() and 0xFF) //domain
            4    -> return 19 //ipv6
            else -> return dft
        }
    }

    fun EVP_BytesToKey(password: ByteArray, key: ByteArray)
    {
        val result = ByteArray(password.size + 16)
        var i = 0
        var md: ByteArray = ByteArray(0)

        while (i < key.size)
        {
            if (i == 0)
            {
                md = md5.digest(password)
            }
            else
            {
                System.arraycopy(md, 0, result, 0, md.size)
                System.arraycopy(password, 0, result, md.size, password.size)
                md = md5.digest(result)
            }
            System.arraycopy(md, 0, key, i, md.size)
            i += md.size
        }
    }

    fun getMD5(src: ByteArray): ByteArray = md5.digest(src)

    fun fillIntAsBytes(i: Int, dst: ByteArray, dstOff: Int)
    {
        dst[dstOff] = i.toByte()
        dst[dstOff + 1] = (i shr 8).toByte()
        dst[dstOff + 2] = (i shr 16).toByte()
        dst[dstOff + 3] = (i shr 24).toByte()
    }

    fun bytesHexDmp(tag: String, bytes: ByteArray)
    {
        val sb = StringBuilder()
        for (b in bytes)
        {
            sb.append("%02X ".format(b))
        }
        Log.e(tag, sb.toString())
    }
    //
    //        fun bufHexDmp(tag: String, bb: ByteBuffer)
    //        {
    //            val sb = StringBuilder()
    //            var st = bb.position()
    //            val cnt = bb.limit()
    //            while (st < cnt)
    //            {
    //                sb.append("%02X ".format(bb.get(st)))
    //                st++
    //            }
    //            Log.e(tag, sb.toString())
    //        }
}
