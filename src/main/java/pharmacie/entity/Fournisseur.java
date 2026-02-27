package pharmacie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    @NotBlank
    private String nom;

    @NonNull
    @Email
    @Column(unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
        name = "fournisseur_categorie",
        joinColumns = @JoinColumn(name = "fournisseur_id"),
        inverseJoinColumns = @JoinColumn(name = "categorie_code")
    )
    @ToString.Exclude
    private Set<Categorie> categories = new HashSet<>();
}
