package cds.adql.validation.parser.adql;

public enum ADQLVersion {
    V2_0,
    V2_1;

    @Override
    public String toString() {
        return name().substring(1).replace('_', '.');
    }

    public static ADQLVersion latest(){
        return V2_1;
    }
}
