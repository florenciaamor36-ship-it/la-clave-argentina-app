package com.example

import com.example.model.DtpAccessRules
import com.example.model.DtpProfilePackage
import com.example.model.RestrictionType
import com.example.model.SshConfig
import com.example.model.TunnelType
import com.example.util.DtpFileManager
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun dtpSerialization_and_deserialization_isCorrect() {
        val originalConfig = SshConfig(
            id = "test-123",
            profileName = "Servidor Premium",
            host = "ssh.tiendassh.com",
            port = "80",
            username = "usuario_vip",
            password = "password123",
            payload = "CONNECT [host_port] HTTP/1.1[crlf]Host: portal.com[crlf][crlf]",
            sniBugHost = "sni.cloud.com",
            tunnelType = TunnelType.SSH_HTTP_CUSTOM
        )

        val rules = DtpAccessRules(
            restrictionType = RestrictionType.VPS_HWID_CHECK,
            remoteValidationUrl = "http://192.168.1.100:8080/hwids.txt",
            allowedDeviceIds = listOf("TS-ABCD-EFGH-1234"),
            isConfigLocked = true,
            creatorNote = "Perfil exclusivo de prueba"
        )

        val pkg = DtpProfilePackage(
            profileName = "Servidor Premium",
            sshConfig = originalConfig,
            accessRules = rules
        )

        val encodedDtp = DtpFileManager.serializeToDtp(pkg)
        assertNotNull(encodedDtp)
        assertTrue(encodedDtp.isNotEmpty())

        val deserialized = DtpFileManager.deserializeFromDtp(encodedDtp)
        assertNotNull(deserialized)
        assertEquals("Servidor Premium", deserialized?.profileName)
        assertEquals("ssh.tiendassh.com", deserialized?.sshConfig?.host)
        assertEquals("80", deserialized?.sshConfig?.port)
        assertEquals("usuario_vip", deserialized?.sshConfig?.username)
        assertEquals("password123", deserialized?.sshConfig?.password)
        assertEquals(TunnelType.SSH_HTTP_CUSTOM, deserialized?.sshConfig?.tunnelType)
        assertEquals(RestrictionType.VPS_HWID_CHECK, deserialized?.accessRules?.restrictionType)
        assertEquals("http://192.168.1.100:8080/hwids.txt", deserialized?.accessRules?.remoteValidationUrl)
        assertTrue(deserialized?.accessRules?.isConfigLocked == true)
        assertEquals("Perfil exclusivo de prueba", deserialized?.accessRules?.creatorNote)
    }

    @Test
    fun dtpExpiration_validation_logic() {
        val futureTime = System.currentTimeMillis() + 100000L
        val pastTime = System.currentTimeMillis() - 100000L

        val validRules = DtpAccessRules(
            restrictionType = RestrictionType.EXPIRATION,
            expirationTimestamp = futureTime
        )
        assertFalse(System.currentTimeMillis() > validRules.expirationTimestamp!!)

        val expiredRules = DtpAccessRules(
            restrictionType = RestrictionType.EXPIRATION,
            expirationTimestamp = pastTime
        )
        assertTrue(System.currentTimeMillis() > expiredRules.expirationTimestamp!!)
    }

    @Test
    fun payloadTagReplacement_isCorrect() {
        val template = "CONNECT [host_port] [protocol][crlf]Host: [host][crlf][crlf]"
        val replaced = template
            .replace("[host_port]", "198.51.100.1:80")
            .replace("[host]", "198.51.100.1")
            .replace("[port]", "80")
            .replace("[protocol]", "HTTP/1.1")
            .replace("[crlf]", "\r\n")

        assertTrue(replaced.contains("CONNECT 198.51.100.1:80 HTTP/1.1\r\n"))
        assertTrue(replaced.contains("Host: 198.51.100.1\r\n\r\n"))
    }

    @Test
    fun tunnelType_allVariants_haveDisplayNameAndDescription() {
        for (type in TunnelType.values()) {
            assertNotNull(type.displayName)
            assertTrue(type.displayName.isNotBlank())
            assertNotNull(type.description)
            assertTrue(type.description.isNotBlank())
        }
    }

    @Test
    fun singleId_and_multiId_matchRules() {
        val testHwid = "TS-1122-3344-5566"
        val singleRule = DtpAccessRules(
            restrictionType = RestrictionType.SINGLE_ID,
            allowedDeviceId = "ts-1122-3344-5566"
        )
        assertEquals(testHwid.uppercase(Locale.ROOT), singleRule.allowedDeviceId?.uppercase(Locale.ROOT))

        val multiRule = DtpAccessRules(
            restrictionType = RestrictionType.MULTI_ID,
            allowedDeviceIds = listOf("TS-AAAA-BBBB-CCCC", "TS-1122-3344-5566")
        )
        assertTrue(multiRule.allowedDeviceIds.map { it.uppercase(Locale.ROOT) }.contains(testHwid.uppercase(Locale.ROOT)))
    }
}
