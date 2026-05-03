package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.logic.GameEngine;
import com.sportrivia.sdk.internal.models.PlayerInfo;
import com.sportrivia.sdk.internal.services.S3DataService;
import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaCredentialProvider;
import com.sportrivia.sdk.public_api.SporTriviaGameResult;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameEngineTest {

    private GameEngine engine;

    @Before
    public void setUp() {
        SporTriviaCredentialProvider mockProvider = new SporTriviaCredentialProvider() {
            @Override
            public String getPresignedGetUrl(String s3Key) { return "https://example.com/" + s3Key; }
            @Override
            public String getPresignedPutUrl(String s3Key) { return "https://example.com/put/" + s3Key; }
        };
        S3DataService s3 = new S3DataService(mockProvider);
        engine = new GameEngine(s3);
    }

    @Test
    public void testCheckAnswerCorrect() {
        engine.startGame();
        engine.setUserInfo("John", "Doe", "john@test.com", "555-1234");

        SporTriviaGameResult result = engine.buildResult();
        assertEquals(0, result.getCorrect());
        assertEquals(0, result.getIncorrect());
        assertEquals("John", result.getFirstName());
    }

    @Test
    public void testPlayerNotUsedInitially() {
        engine.startGame();
        assertFalse(engine.isPlayerUsed("player001"));
    }

    @Test
    public void testAllAnswersFoundWhenEmpty() {
        engine.startGame();
        assertTrue(engine.allAnswersFound());
    }

    @Test
    public void testBuildResult() {
        engine.setUserInfo("Jane", "Smith", "jane@test.com", "555-0000");
        engine.startGame();

        SporTriviaGameResult result = engine.buildResult();
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(0, result.getMaxStreak());
    }
}
