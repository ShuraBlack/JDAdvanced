package de.shurablack.core.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalDataTest {

    @Test
    void clearRemovesAllStoredData() {
        LocalData.saveTempValue("key", "value");
        LocalData.clear();
        assertNull(LocalData.getTmp("key"));
    }

    @Test
    void saveTempValueStoresKeyValuePair() {
        LocalData.saveTempValue("key", "value");
        assertEquals("value", LocalData.getTmp("key"));
    }

    @Test
    void deleteTempValueRemovesKeyValuePair() {
        LocalData.saveTempValue("key", "value");
        LocalData.deleteTempValue("key");
        assertNull(LocalData.getTmp("key"));
    }

    @Test
    void fetchCategoriesReturnsFalseForNullGuild() {
        assertFalse(LocalData.fetchCategories(null));
    }

    @Test
    void fetchCategoriesStoresCategoryData() {
        Guild mockGuild = mock(Guild.class);
        Category mockCategory = mock(Category.class);
        when(mockCategory.getId()).thenReturn("123");
        when(mockCategory.getName()).thenReturn("TestCategory");
        when(mockGuild.getCategories()).thenReturn(Collections.singletonList(mockCategory));

        LocalData.fetchCategories(mockGuild);
        assertEquals("123", LocalData.getCategoryID("TestCategory"));
    }

    @Test
    void fetchChannelsReturnsFalseForNullGuild() {
        assertFalse(LocalData.fetchChannels(null));
    }

    @Test
    void fetchChannelsStoresChannelData() {
        Guild mockGuild = mock(Guild.class);
        GuildChannel mockChannel = mock(GuildChannel.class);
        when(mockChannel.getId()).thenReturn("456");
        when(mockChannel.getName()).thenReturn("TestChannel");
        when(mockGuild.getChannels()).thenReturn(Collections.singletonList(mockChannel));

        LocalData.fetchChannels(mockGuild);
        assertEquals("456", LocalData.getChannelID("TestChannel"));
    }

    @Test
    void fetchEmojisReturnsFalseForNullGuild() {
        assertFalse(LocalData.fetchEmojis(null));
    }

    @Test
    void fetchEmojisStoresEmojiData() {
        Guild mockGuild = mock(Guild.class);
        RichCustomEmoji mockEmoji = mock(RichCustomEmoji.class);
        when(mockEmoji.getName()).thenReturn("TestEmoji");
        when(mockEmoji.getAsMention()).thenReturn(":TestEmoji:");
        when(mockGuild.getEmojis()).thenReturn(Collections.singletonList(mockEmoji));

        LocalData.fetchEmojis(mockGuild);
        assertEquals(":TestEmoji:", LocalData.getEmojiID("TestEmoji"));
    }

    @Test
    void fetchRolesReturnsFalseForNullGuild() {
        assertFalse(LocalData.fetchRoles(null));
    }

    @Test
    void getTmpReturnsNullForNonExistentKey() {
        assertNull(LocalData.getTmp("nonExistentKey"));
    }

    @Test
    void getCategoryIDReturnsNullForNonExistentCategory() {
        assertNull(LocalData.getCategoryID("nonExistentCategory"));
    }

    @Test
    void getChannelIDReturnsNullForNonExistentChannel() {
        assertNull(LocalData.getChannelID("nonExistentChannel"));
    }

    @Test
    void getMessageIDReturnsNullForNonExistentMessage() {
        assertNull(LocalData.getMessageID("nonExistentMessage"));
    }

    @Test
    void getWebHookLinkReturnsNullForNonExistentWebhook() {
        assertNull(LocalData.getWebHookLink("nonExistentWebhook"));
    }

    @Test
    void getEmojiIDReturnsNullForNonExistentEmoji() {
        assertNull(LocalData.getEmojiID("nonExistentEmoji"));
    }

    @Test
    void getRoleIDReturnsNullForNonExistentRole() {
        assertNull(LocalData.getRoleID("nonExistentRole"));
    }
}