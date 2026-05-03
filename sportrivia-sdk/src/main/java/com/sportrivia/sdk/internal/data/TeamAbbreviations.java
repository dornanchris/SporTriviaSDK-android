package com.sportrivia.sdk.internal.data;

import com.sportrivia.sdk.public_api.Sport;

import java.util.HashMap;
import java.util.Map;

/**
 * Consolidated team abbreviation dictionaries for all sports.
 */
public class TeamAbbreviations {

    public static Map<String, String> getDictionary(Sport sport) {
        switch (sport) {
            case MLB: return baseball();
            case NBA: return basketball();
            case NFL: return football();
            case NHL: case AHL: case ECHL: return hockey();
            default: return baseball();
        }
    }

    public static String getTeamName(String abbreviation, Sport sport) {
        Map<String, String> dict = getDictionary(sport);
        for (Map.Entry<String, String> entry : dict.entrySet()) {
            if (entry.getValue().equals(abbreviation)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static Map<String, String> baseball() {
        Map<String, String> m = new HashMap<>();
        m.put("Arizona Diamondbacks", "ARI"); m.put("Atlanta Braves", "ATL");
        m.put("Baltimore Orioles", "BAL"); m.put("Boston Red Sox", "BOS");
        m.put("Chicago Cubs", "CHC"); m.put("Chicago White Sox", "CHW");
        m.put("Cincinnati Reds", "CIN"); m.put("Cleveland Guardians", "CLE");
        m.put("Colorado Rockies", "COL"); m.put("Detroit Tigers", "DET");
        m.put("Houston Astros", "HOU"); m.put("Kansas City Royals", "KCR");
        m.put("Los Angeles Angels", "LAA"); m.put("Los Angeles Dodgers", "LAD");
        m.put("Miami Marlins", "MIA"); m.put("Milwaukee Brewers", "MIL");
        m.put("Minnesota Twins", "MIN"); m.put("New York Mets", "NYM");
        m.put("New York Yankees", "NYY"); m.put("Oakland Athletics", "OAK");
        m.put("Philadelphia Phillies", "PHI"); m.put("Pittsburgh Pirates", "PIT");
        m.put("San Diego Padres", "SDP"); m.put("San Francisco Giants", "SFG");
        m.put("Seattle Mariners", "SEA"); m.put("St. Louis Cardinals", "STL");
        m.put("Tampa Bay Rays", "TBR"); m.put("Texas Rangers", "TEX");
        m.put("Toronto Blue Jays", "TOR"); m.put("Washington Nationals", "WSN");
        m.put("500 Career Home Runs", "500careerHR");
        m.put("400 Career Home Runs", "400careerHR");
        m.put("3000 Career Hits", "3000careerHits");
        m.put("2000 Career Hits", "2000careerHits");
        m.put("300 Career Wins", "300careerW");
        m.put("200 Career Wins", "200careerW");
        m.put("100 Career Wins", "100careerW");
        m.put("40 HR Season", "40HRseason");
        m.put("50 HR Season", "50HRseason");
        m.put("200 Hit Season", "200Hseason");
        m.put("20 Win Season", "20Wseason");
        return m;
    }

    private static Map<String, String> basketball() {
        Map<String, String> m = new HashMap<>();
        m.put("Atlanta Hawks", "ATL"); m.put("Boston Celtics", "BOS");
        m.put("Brooklyn Nets", "BRK"); m.put("Charlotte Hornets", "CHO");
        m.put("Chicago Bulls", "CHI"); m.put("Cleveland Cavaliers", "CLE");
        m.put("Dallas Mavericks", "DAL"); m.put("Denver Nuggets", "DEN");
        m.put("Detroit Pistons", "DET"); m.put("Golden State Warriors", "GSW");
        m.put("Houston Rockets", "HOU"); m.put("Indiana Pacers", "IND");
        m.put("Los Angeles Clippers", "LAC"); m.put("Los Angeles Lakers", "LAL");
        m.put("Memphis Grizzlies", "MEM"); m.put("Miami Heat", "MIA");
        m.put("Milwaukee Bucks", "MIL"); m.put("Minnesota Timberwolves", "MIN");
        m.put("New Orleans Pelicans", "NOP"); m.put("New York Knicks", "NYK");
        m.put("Oklahoma City Thunder", "OKC"); m.put("Orlando Magic", "ORL");
        m.put("Philadelphia 76ers", "PHI"); m.put("Phoenix Suns", "PHO");
        m.put("Portland Trail Blazers", "POR"); m.put("Sacramento Kings", "SAC");
        m.put("San Antonio Spurs", "SAS"); m.put("Toronto Raptors", "TOR");
        m.put("Utah Jazz", "UTA"); m.put("Washington Wizards", "WAS");
        m.put("10000 Career Points", "10kcareerPTS");
        m.put("5000 Career Points", "5kcareerPTS");
        m.put("1500 Point Season", "1500PTSseason");
        m.put("1000 Point Season", "1000PTSseason");
        return m;
    }

    private static Map<String, String> football() {
        Map<String, String> m = new HashMap<>();
        m.put("Arizona Cardinals", "CRD"); m.put("Atlanta Falcons", "ATL");
        m.put("Baltimore Ravens", "RAV"); m.put("Buffalo Bills", "BUF");
        m.put("Carolina Panthers", "CAR"); m.put("Chicago Bears", "CHI");
        m.put("Cincinnati Bengals", "CIN"); m.put("Cleveland Browns", "CLE");
        m.put("Dallas Cowboys", "DAL"); m.put("Denver Broncos", "DEN");
        m.put("Detroit Lions", "DET"); m.put("Green Bay Packers", "GNB");
        m.put("Houston Texans", "HTX"); m.put("Indianapolis Colts", "CLT");
        m.put("Jacksonville Jaguars", "JAX"); m.put("Kansas City Chiefs", "KAN");
        m.put("Las Vegas Raiders", "RAI"); m.put("Los Angeles Chargers", "SDG");
        m.put("Los Angeles Rams", "RAM"); m.put("Miami Dolphins", "MIA");
        m.put("Minnesota Vikings", "MIN"); m.put("New England Patriots", "NWE");
        m.put("New Orleans Saints", "NOR"); m.put("New York Giants", "NYG");
        m.put("New York Jets", "NYJ"); m.put("Philadelphia Eagles", "PHI");
        m.put("Pittsburgh Steelers", "PIT"); m.put("San Francisco 49ers", "SFO");
        m.put("Seattle Seahawks", "SEA"); m.put("Tampa Bay Buccaneers", "TAM");
        m.put("Tennessee Titans", "OTI"); m.put("Washington Commanders", "WAS");
        m.put("10k career Passing Yards", "10kcareerPassingYards");
        m.put("100 career Passing TDs", "100careerPassingTDs");
        m.put("10k career Rushing Yards", "10kcareerRushingYards");
        m.put("100 career Sacks", "100careerSacks");
        return m;
    }

    private static Map<String, String> hockey() {
        Map<String, String> m = new HashMap<>();
        m.put("Anaheim Ducks", "ANA"); m.put("Arizona Coyotes", "ARI");
        m.put("Boston Bruins", "BOS"); m.put("Buffalo Sabres", "BUF");
        m.put("Calgary Flames", "CGY"); m.put("Carolina Hurricanes", "CAR");
        m.put("Chicago Blackhawks", "CHI"); m.put("Colorado Avalanche", "COL");
        m.put("Columbus Blue Jackets", "CBJ"); m.put("Dallas Stars", "DAL");
        m.put("Detroit Red Wings", "DET"); m.put("Edmonton Oilers", "EDM");
        m.put("Florida Panthers", "FLA"); m.put("Los Angeles Kings", "LAK");
        m.put("Minnesota Wild", "MIN"); m.put("Montreal Canadiens", "MTL");
        m.put("Nashville Predators", "NSH"); m.put("New Jersey Devils", "NJD");
        m.put("New York Islanders", "NYI"); m.put("New York Rangers", "NYR");
        m.put("Ottawa Senators", "OTT"); m.put("Philadelphia Flyers", "PHI");
        m.put("Pittsburgh Penguins", "PIT"); m.put("San Jose Sharks", "SJS");
        m.put("Seattle Kraken", "SEA"); m.put("St. Louis Blues", "STL");
        m.put("Tampa Bay Lightning", "TBL"); m.put("Toronto Maple Leafs", "TOR");
        m.put("Utah Mammoth", "UTA"); m.put("Vancouver Canucks", "VAN");
        m.put("Vegas Golden Knights", "VEG"); m.put("Washington Capitals", "WSH");
        m.put("Winnipeg Jets", "WPG");
        m.put("500 Career Goals", "500careerG");
        m.put("1000 Career Points", "1000careerPTS");
        m.put("100 Point Season", "100PTSseason");
        m.put("50 Goal Season", "50Gseason");
        m.put("30 Goal Season", "30Gseason");
        m.put("300 Career Wins", "300careerW");
        m.put("Bridgeport Islanders", "BPI"); m.put("Hershey Bears", "HSB");
        m.put("Toronto Marlies", "TOM"); m.put("Providence Bruins", "PRV");
        m.put("Toledo Walleye", "TOW"); m.put("Florida Everblades", "FLE");
        return m;
    }
}
