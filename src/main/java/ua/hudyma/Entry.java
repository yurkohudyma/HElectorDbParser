package ua.hudyma;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String birth;
    private String name;
    private String surname;
    private String middleName;
    private String address;
}
