package org.kuettler.mathapp;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

class Stats {
    public static final String FILENAME = "stat.json";

    public static class GameList {
        private LinkedList<Game> games;
        private Game best;

        GameList() {
            games = new LinkedList<Game>();
        }

        public void addGame(Game game) {
            if (best == null || best.points() < game.points()) {
                best = game;
            }
            games.add(game);
        }

        public boolean isEmpty() {
            return games.isEmpty();
        }
        public int size() {
            return games.size();
        }
        public Game getLast() {
            return games.getLast();
        }

        public Iterable<Game> asDescendingIterable() {
            return new Iterable<Game>() {
                public Iterator<Game> iterator() {
                    return games.descendingIterator();
                }
            };
        }

        public Game getBestGame() {
            return best;
        }

        public boolean isBest(Game game) {
            return game.points() == best.points();
        }

        public boolean isLastBest() {
            return isBest(getLast());
        }

        public int pointRecord() {
            if (best == null)
                return -1;
            return best.points();
        }

        public JSONArray toJSONArray() throws JSONException {
            JSONArray result = new JSONArray();
            for (Game game : games) {
                result.put(game.toJSONObject());
            }
            return result;
        }

        public static GameList fromJSONArray(JSONArray glJSON,
                                             Exercise.Level level,
                                             MathActivity.Mode mode) throws JSONException {
            GameList result = new GameList();
            for (int index = 0; index < glJSON.length(); ++index) {
                result.addGame(Game.fromJSONObject(glJSON.getJSONObject(index),
                                                   level, mode));
            }
            return result;
        }
    }

    public static class Game {
        private final Exercise.Level level;
        private final MathActivity.Mode mode;
        private final int rightAnswers;
        private final int wrongAnswers;
        private final float secondsPerAnswer;
        private final Date date;

        private static final String RIGHT_ANSWERS = "rightAnswers";
        private static final String WRONG_ANSWERS = "wrongAnswers";
        private static final String SECONDS_PER_ANSWER = "secondsPerAnswer";
        private static final String DATE = "date";

        public static class Builder {
            private final Exercise.Level level;
            private final MathActivity.Mode mode;

            private Date date;
            private int rightAnswers;
            private int wrongAnswers;
            private float secondsPerAnswer;
            public Builder(Exercise.Level level, MathActivity.Mode mode) {
                this.level = level;
                this.mode = mode;
                date = new Date();
            }

            public Builder rightAnswers(int val) {
                rightAnswers = val;
                return this;
            }
            public Builder wrongAnswers(int val) {
                wrongAnswers = val;
                return this;
            }
            public Builder secondsPerAnswer(float val) {
                secondsPerAnswer = val;
                return this;
            }
            public Builder date(Date val) {
                date = val;
                return this;
            }
            public Builder date(long val) {
                date = new Date(val);
                return this;
            }

            public Game build() {
                return new Game(this);
            }
        }

        private Game(Builder builder) {
            level = builder.level;
            mode = builder.mode;
            rightAnswers = builder.rightAnswers;
            wrongAnswers = builder.wrongAnswers;
            secondsPerAnswer = builder.secondsPerAnswer;
            date = builder.date;
        }

        public Exercise.Level level() {
            return this.level;
        }
        public MathActivity.Mode mode() {
            return this.mode;
        }
        public int rightAnswers() {
            return this.rightAnswers;
        }
        public int wrongAnswers() {
            return this.wrongAnswers;
        }
        public float secondsPerAnswer() {
            return this.secondsPerAnswer;
        }
        public Date date() {
            return this.date;
        }

        public int points() {
            return Math.max(0, rightAnswers - wrongAnswers);
        }

        public JSONObject toJSONObject() throws JSONException {
            return new JSONObject()
                .put(RIGHT_ANSWERS, rightAnswers)
                .put(WRONG_ANSWERS, wrongAnswers)
                .put(SECONDS_PER_ANSWER, secondsPerAnswer)
                .put(DATE, date.getTime());
        }

        public static Game fromJSONObject(JSONObject gameJSON,
                                          Exercise.Level level,
                                          MathActivity.Mode mode) throws JSONException {
            return new Builder(level, mode)
                .rightAnswers(gameJSON.getInt(RIGHT_ANSWERS))
                .wrongAnswers(gameJSON.getInt(WRONG_ANSWERS))
                .secondsPerAnswer((float) gameJSON.getDouble(SECONDS_PER_ANSWER))
                .date(gameJSON.getLong(DATE)).build();
        }

        @Override
        public String toString() {
            try {
                return toJSONObject().toString(2);
            } catch (JSONException e) {
                Log.d(MathActivity.TAG, e.toString());
                return null;
            }
        }
    }

    private static final Stats INSTANCE = new Stats();
    private Map<Exercise.Level, Map<MathActivity.Mode, GameList>> data;

    private Stats() {
        data = new HashMap<Exercise.Level, Map<MathActivity.Mode, GameList>>();
        for (Exercise.Level level : Exercise.Level.values()) {
            data.put(level, new HashMap<MathActivity.Mode, GameList>());
            for (MathActivity.Mode mode : MathActivity.Mode.values()) {
                data.get(level).put(mode, new GameList());
            }
        }
    }

    public static Stats getInstance() {
        return INSTANCE;
    }

    // Returns _previous_ point record
    public int addGame(Game game) {
        GameList gl = getGameList(game);
        int record = gl.pointRecord();
        gl.addGame(game);
        return record;
    }

    public boolean isBest(Game game) {
        return getGameList(game).isBest(game);
    }

    GameList getGameList(Exercise.Level level, MathActivity.Mode mode) {
        return data.get(level).get(mode);
    }

    protected GameList getGameList(Game game) {
        return getGameList(game.level(), game.mode());
    }

    public boolean loadJSONObject(JSONObject statJSON) {
        try {
            for (Exercise.Level level : Exercise.Level.values()) {
                JSONObject levelJSON = statJSON.getJSONObject(level.name());
                for (MathActivity.Mode mode : MathActivity.Mode.values()) {
                    JSONArray glJSON = levelJSON.getJSONArray(mode.name());
                    data.get(level).put(mode,
                                        GameList.fromJSONArray(glJSON, level, mode));
                }
            }
        } catch (JSONException e) {
            Log.d(MathActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public boolean load(android.content.Context context) {
        InputStream stream;
        try {
            stream = context.openFileInput(Stats.FILENAME);

            int count;
            byte[] buffer = new byte[1024];

            ByteArrayOutputStream byteStream =
                new ByteArrayOutputStream(stream.available());

            while (true) {
                count = stream.read(buffer);
                if (count <= 0)
                    break;
                byteStream.write(buffer, 0, count);
            }

            Log.d(MathActivity.TAG, byteStream.toString());
            return loadJSONObject(new JSONObject(byteStream.toString()));
        } catch (Exception e) {
            Log.d(MathActivity.TAG, e.toString());
            return false;
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        for (Exercise.Level level : Exercise.Level.values()) {
            JSONObject levelJSON = new JSONObject();
            for (MathActivity.Mode mode : MathActivity.Mode.values()) {
                levelJSON.put(mode.name(),
                              data.get(level).get(mode).toJSONArray());
            }
            result.put(level.name(), levelJSON);
        }
        return result;
    }

    public boolean save(android.content.Context context) {
        OutputStream stream;
        try {
            stream = context.openFileOutput(Stats.FILENAME,
                                            android.content.Context.MODE_PRIVATE);
            stream.write(toJSONObject().toString().getBytes());
            stream.close();
        } catch (Exception e) {
            Log.d(MathActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
}
