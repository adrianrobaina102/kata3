package software.ulpgc.katas.adapters;

public class UserDto {
    public int id;
    public String name;
    public String email;
    public Address address;

    private static class Address {
        public String street;
        public String suite;
        public String city;
        public String zipcode;
        public Geo geo;

        private static class Geo {
            public String lat;
            public String lng;
        }
    }
    public String phone;
    public String website;
    public Company company;

    private static class Company {
        public String name;
        public String catchPhrase;
        public String bs;
    }
}
