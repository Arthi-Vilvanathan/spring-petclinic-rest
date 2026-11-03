package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Verified mapping between an authenticated {@link User} and an {@link Owner}.
 * <p>
 * The mapping is explicit and persisted; ownership is never inferred from names.
 */
@Entity
@Table(name = "owner_user_links", uniqueConstraints = {
    @UniqueConstraint(name = "uk_owner_user_links_username", columnNames = { "username" }),
    @UniqueConstraint(name = "uk_owner_user_links_owner_id", columnNames = { "owner_id" })
})
public class OwnerUserLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    private User user;

    @Column(name = "verified", nullable = false)
    private boolean verified = true;

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
