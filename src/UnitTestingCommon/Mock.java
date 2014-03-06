package UnitTestingCommon;

public class Mock {
    private String name;

    public Mock(String name) {
            this.name = name;
    }

    public String getName() {
            return name;
    }
    
    public String getAgentName() {
    	return name;
    }

    public String toString() {
            return this.getClass().getName() + ": " + name;
    }

}