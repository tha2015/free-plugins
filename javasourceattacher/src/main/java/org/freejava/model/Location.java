package org.freejava.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bundleId; // ID of corresponding bundle
    private String url; // URl where the bundle can be downloaded

    // Accessors for the fields.  JPA doesn't use these, but your application does.

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

	public Long getBundleId() {
		return bundleId;
	}
	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}