package TinyTM.contention;

public enum CMEnum {
    Passive(0),
    Polite(1),
    Karma(2),
    Polka(3),
    Timestamp(4),
    Kindergarten(5),
    Less(6),
    Aggressive(7);

    private int id;

    CMEnum(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }

    public static CMEnum fromId(int id) {
      for (CMEnum type : values()) {
        if (type.getId() == id) {
          return type;
        }
      }
      return Passive;
    }
  }