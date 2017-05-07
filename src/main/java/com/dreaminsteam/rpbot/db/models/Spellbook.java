package com.dreaminsteam.rpbot.db.models;

import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "spellbook")
public class Spellbook {
	
	public static final int POINTS_PER_BONUS = 3;
	public static final int DEFAULT_MAX_BONUS = 3;
	public static final int MAX_ATTEMPTS_PER_DAY = 3;
	
	@DatabaseField(foreign=true, columnName="player_id", foreignAutoRefresh=true)
	private Player player;
	
	@DatabaseField(foreign=true, columnName="spell_id", foreignAutoRefresh=true)
	private Spell spell;
	
	@DatabaseField(generatedId = true) private Long id;
	@DatabaseField private int currentIndividualModifierPoints = 0;
	@DatabaseField private int castAttemptsToday = 0;
	@DatabaseField private Date lastCastAttempt;
	@DatabaseField private boolean practiceSuccessful;
	
	public Spellbook(){
		//ORMLite requires an empty constructor
	}
	
	public Spellbook(Player player, Spell spell){
		this.player = player;
		this.spell = spell;
	}
	
	public int getIndividualModifier(){
		return getIndividualModifier(DEFAULT_MAX_BONUS);
	}
	
	public int getIndividualModifier(int maxModifier){
		return Math.min(maxModifier, currentIndividualModifierPoints/POINTS_PER_BONUS);
	}
	
	public int getProgressTowardsNextBonus(){
		return currentIndividualModifierPoints % POINTS_PER_BONUS;
	}
	
	public boolean hasPracticedToday(Date today){
		if(lastCastAttempt == null){
			return false;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(lastCastAttempt);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		
		if(calendar.after(today)){
			return true;
		}else{
			return false;
		}
	}
	
	public void practiceSpell(boolean success){
		Date now = new Date();
		boolean practiced = false;
		if(hasPracticedToday(now)){
			if(!practiceSuccessful && castAttemptsToday < MAX_ATTEMPTS_PER_DAY){
				castAttemptsToday++;
				practiced = true;
			}
		}else{
			castAttemptsToday = 1;
			practiced = true;
		}
		
		if(practiced){
			lastCastAttempt = now;
			practiceSuccessful = success;
			if(practiceSuccessful){
				currentIndividualModifierPoints++;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + ((spell == null) ? 0 : spell.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Spellbook other = (Spellbook) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (spell == null) {
			if (other.spell != null)
				return false;
		} else if (!spell.equals(other.spell))
			return false;
		return true;
	}

}