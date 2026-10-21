package de.shurablack.core.builder;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class BuilderTest {

    @Test
    void createWithoutInitThrowsInsteadOfExiting() {
        assertThrows(InitException.class, () -> JDAUtilBuilder.create(mock(JDABuilder.class), null));
        assertThrows(InitException.class, () -> ShardUtilBuilder.create(mock(DefaultShardManagerBuilder.class), null));
    }
}
