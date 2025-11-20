import java.util.*;
import java.util.concurrent.TimeUnit;

public class SnakesAndLadders
{
    private final static Set<String> VALID_COLORS = Set.of(
            "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black",
            "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse",
            "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan",
            "darkgoldenrod", "darkgray", "darkgrey", "darkgreen", "darkkhaki", "darkmagenta",
            "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
            "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink",
            "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen",
            "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "grey", "green",
            "greenyellow", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender",
            "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
            "lightgoldenrodyellow", "lightgray", "lightgrey", "lightgreen", "lightpink", "lightsalmon",
            "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue",
            "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine",
            "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue",
            "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream",
            "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange",
            "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred",
            "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple",
            "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell",
            "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen",
            "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white",
            "whitesmoke", "yellow", "yellowgreen"
    );

    private final static int BOARD_SIZE = 100;

    private static final List<Snake> snakes = new ArrayList<>();
    private static final List<Ladder> ladders = new ArrayList<>();
    private static final List<Player> players = new ArrayList<>();

    static void main(String[] args)
    {
        if(args.length < 1)
        {
            System.out.println("please provide at least one player");
            return;
        }

        int numSnakes = 10;
        int numLadders = 10;


        generatePlayers(args);
        System.out.println("Player Order:");
        for(Player p : players)
            System.out.println(p.color + ": " + p.order);
        generateBoard(numSnakes, numLadders);
        playGame();

    }

    public static int rollD6()
    {
        Random r = new Random();
        return r.nextInt(6) + 1;
    }

    private static void generatePlayers(String[] args)
    {
        for(String color : args)
        {
            if(!VALID_COLORS.contains(color.toLowerCase()))
                System.out.println("Invalid color: " + color + ". No player added.");
            else if (players.stream().anyMatch(p -> p.color.equals(color.toLowerCase())))
                System.out.println("Player " + color + " already exists.");
            else
                players.add(new Player(color.toLowerCase()));
        }
        for(Player player : players)
            player.breakTies(players);
        players.sort(Comparator.comparingDouble(Player::getOrder).reversed());
    }

    private static void generateBoard(int numSnakes, int numLadders)
    {
        for(int i = 0; i < numSnakes; i++)
        {
            Random r = new Random();
            int head;
            do
                head = r.nextInt(BOARD_SIZE - 10) + 10;
            while(positionOccupied(head));
            int tail;
            do
                tail = r.nextInt(head - 2) + 2;
            while(positionOccupied(tail));
            snakes.add(new Snake(head, tail));
        }

        for(int i = 0; i < numLadders; i++)
        {
            Random r = new Random();
            int top;
            do
                top = r.nextInt(BOARD_SIZE - 10) + 10;
            while(positionOccupied(top));
            int base;
            do
                base = r.nextInt(top - 3) + 3;
            while(positionOccupied(base));
            ladders.add(new Ladder(base, top));
        }
    }

    private static boolean positionOccupied(int position)
    {
        Optional<Snake> occupyingSnake = snakes.stream()
                .filter(s -> s.head == position || s.tail == position)
                .findFirst();

        if(!occupyingSnake.isEmpty())
            return true;

        Optional<Ladder> occupyingLadder = ladders.stream()
                .filter(l -> l.base == position || l.top == position)
                .findFirst();

        return !occupyingLadder.isEmpty();
    }

    private static void movePlayer(Player player)
    {
        int dieRoll = rollD6();
        player.position += dieRoll;
        System.out.print(player.color + " rolled " + dieRoll);

        if(player.position > BOARD_SIZE)
        {
            player.position = BOARD_SIZE;
            System.out.println(", moved to " + player.position);
            return;
        }

        for(Snake snake : snakes)
        {
            if(snake.head == player.position)
            {
                System.out.println(", landed on snake at " + snake.head + ", slipped down to " + snake.tail);
                player.position = snake.tail;
                return;
            }
        }
        for(Ladder ladder : ladders)
        {
            if(ladder.base == player.position)
            {
                System.out.println(", landed on ladder at " + ladder.base + ", climbed up to " + ladder.top);
                player.position = ladder.top;
                return;
            }
        }
        System.out.println(", moved to " + player.position);
    }

    private static void playGame()
    {
        while(true)
        {
            for(Player p : players)
            {
                movePlayer(p);
                if(p.position >= BOARD_SIZE)
                {
                    System.out.println(p.color + " Wins!");
                    return;
                }

                delayNextMove();
            }
        }
    }

    private static void delayNextMove()
    {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class Player
{
    final String color;

    double order;

    int position;

    public Player(String color)
    {
        this.color = color;
        this.order = SnakesAndLadders.rollD6();
        this.position = 1;
    }

    public double getOrder()
    {
        return order;
    }

    public void addTieBreaker()
    {
        String orderString = String.valueOf(order);
        orderString += SnakesAndLadders.rollD6();
        order = Double.parseDouble(orderString);
    }

    public void breakTies(List<Player> otherPlayers)
    {
        boolean brokeTies = false;
        for(Player player : otherPlayers)
        {
            if(player.color.equals(this.color))
                //color is unique, if color matches this is the current player, skip checking for ties.
                continue;
            if(player.order == this.order)
            {
                player.addTieBreaker();
                brokeTies = true;
            }
        }
        if(brokeTies)
        {
            this.addTieBreaker();
            this.breakTies(otherPlayers);
        }
    }


}

class Snake
{
    final int head;
    final int tail;

    public Snake(int head, int tail)
    {
        if(tail > head)
            throw new IllegalStateException("Snake cannot have tail greater than its head");

        this.head = head;
        this.tail = tail;
    }
}

class Ladder
{
    final int base;
    final int top;

    public Ladder(int base, int top)
    {
        if(base > top)
            throw new IllegalStateException("Ladder cannot have base greater than its top");

        this.base = base;
        this.top = top;
    }
}

