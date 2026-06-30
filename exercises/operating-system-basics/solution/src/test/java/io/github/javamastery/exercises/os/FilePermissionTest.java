package io.github.javamastery.exercises.os;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FilePermission")
class FilePermissionTest {
    @Test
    @DisplayName("解析符号权限并转换为八进制")
    void parsesSymbolicPermissionAndConvertsToOctal() {
        FilePermission permission = FilePermission.parse("rwxr-x---");

        assertTrue(permission.canOwnerRead());
        assertTrue(permission.canOwnerWrite());
        assertTrue(permission.canOwnerExecute());
        assertTrue(permission.canGroupRead());
        assertFalse(permission.canGroupWrite());
        assertTrue(permission.canGroupExecute());
        assertFalse(permission.canOthersRead());
        assertEquals("750", permission.toOctal());
    }

    @Test
    @DisplayName("判断文件是否任何人可执行")
    void detectsExecutablePermission() {
        assertFalse(FilePermission.parse("rw-r--r--").isExecutableByAnyone());
        assertTrue(FilePermission.parse("rwx------").isExecutableByAnyone());
    }

    @Test
    @DisplayName("拒绝长度或字符不合法的权限文本")
    void rejectsInvalidPermissionText() {
        assertThrows(IllegalArgumentException.class, () -> FilePermission.parse("rwx"));
        assertThrows(IllegalArgumentException.class, () -> FilePermission.parse("rwxr-x--z"));
    }
}
