package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.sql.Clob;

/**
 * StgMbCssId generated by hbm2java
 */
public class StgMbCssId implements java.io.Serializable {

	private Integer impId;
	private String name;
	private Clob css;
	private Byte impNeu;

	public StgMbCssId() {
	}

	public StgMbCssId(Integer impId, String name, Clob css, Byte impNeu) {
		this.impId = impId;
		this.name = name;
		this.css = css;
		this.impNeu = impNeu;
	}

	public Integer getImpId() {
		return this.impId;
	}

	public void setImpId(Integer impId) {
		this.impId = impId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Clob getCss() {
		return this.css;
	}

	public void setCss(Clob css) {
		this.css = css;
	}

	public Byte getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Byte impNeu) {
		this.impNeu = impNeu;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof StgMbCssId))
			return false;
		StgMbCssId castOther = (StgMbCssId) other;

		return ((this.getImpId() == castOther.getImpId()) || (this.getImpId() != null
				&& castOther.getImpId() != null && this.getImpId().equals(
				castOther.getImpId())))
				&& ((this.getName() == castOther.getName()) || (this.getName() != null
						&& castOther.getName() != null && this.getName()
						.equals(castOther.getName())))
				&& ((this.getCss() == castOther.getCss()) || (this.getCss() != null
						&& castOther.getCss() != null && this.getCss().equals(
						castOther.getCss())))
				&& ((this.getImpNeu() == castOther.getImpNeu()) || (this
						.getImpNeu() != null && castOther.getImpNeu() != null && this
						.getImpNeu().equals(castOther.getImpNeu())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getImpId() == null ? 0 : this.getImpId().hashCode());
		result = 37 * result
				+ (getName() == null ? 0 : this.getName().hashCode());
		result = 37 * result
				+ (getCss() == null ? 0 : this.getCss().hashCode());
		result = 37 * result
				+ (getImpNeu() == null ? 0 : this.getImpNeu().hashCode());
		return result;
	}

}