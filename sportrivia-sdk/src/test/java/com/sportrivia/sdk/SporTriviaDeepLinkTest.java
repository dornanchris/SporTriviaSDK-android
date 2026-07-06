package com.sportrivia.sdk;

import com.sportrivia.sdk.public_api.Sport;
import com.sportrivia.sdk.public_api.SporTriviaDeepLink;

import org.junit.Test;

import static org.junit.Assert.*;

public class SporTriviaDeepLinkTest {

    @Test
    public void testParsePartnerSchemeLink() {
        SporTriviaDeepLink link = SporTriviaDeepLink.parse("partnerapp://sportrivia/custom/NYI_Top5A?info=nhl");

        assertNotNull(link);
        assertEquals("NYI_Top5A", link.getGameId());
        assertEquals(Sport.NHL, link.getSport());
    }

    @Test
    public void testParseSporTriviaAppLink() {
        SporTriviaDeepLink link = SporTriviaDeepLink.parse("sportrivia://custom/BOS_NYY?info=mlb");

        assertNotNull(link);
        assertEquals("BOS_NYY", link.getGameId());
        assertEquals(Sport.MLB, link.getSport());
    }

    @Test
    public void testParseAppLinkForm() {
        SporTriviaDeepLink link = SporTriviaDeepLink.parse(
                "https://sportrivia-app.com/sdk/r/islanders/abc-123?game=NYI_Top5A&info=nhl");

        assertNotNull(link);
        assertEquals("NYI_Top5A", link.getGameId());
        assertEquals(Sport.NHL, link.getSport());
    }

    @Test
    public void testParseAppLinkWithoutGameParamIsRejected() {
        assertNull(SporTriviaDeepLink.parse("https://sportrivia-app.com/sdk/r/islanders/abc-123?info=nhl"));
    }

    @Test
    public void testParseStripsJsonSuffix() {
        SporTriviaDeepLink link = SporTriviaDeepLink.parse("partnerapp://sportrivia/custom/NYI_Top5A.json?info=nhl");

        assertNotNull(link);
        assertEquals("NYI_Top5A", link.getGameId());
    }

    @Test
    public void testParseIgnoresIntentFragment() {
        SporTriviaDeepLink link = SporTriviaDeepLink.parse(
                "intent://sportrivia/custom/LAL_BOS?info=nba#Intent;scheme=partnerapp;package=com.example;end");

        assertNotNull(link);
        assertEquals("LAL_BOS", link.getGameId());
        assertEquals(Sport.NBA, link.getSport());
    }

    @Test
    public void testParseRejectsUnknownSport() {
        assertNull(SporTriviaDeepLink.parse("partnerapp://sportrivia/custom/NYI_Top5A?info=cricket"));
    }

    @Test
    public void testParseRejectsMissingSport() {
        assertNull(SporTriviaDeepLink.parse("partnerapp://sportrivia/custom/NYI_Top5A"));
    }

    @Test
    public void testParseRejectsNonGameLinks() {
        assertNull(SporTriviaDeepLink.parse("partnerapp://sportrivia/settings?info=nhl"));
        assertNull(SporTriviaDeepLink.parse("partnerapp://sportrivia/custom?info=nhl"));
        assertNull(SporTriviaDeepLink.parse("not a url"));
        assertNull(SporTriviaDeepLink.parse((String) null));
    }
}
