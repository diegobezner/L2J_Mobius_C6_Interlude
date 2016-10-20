/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.model.actor.knownlist;

import java.util.concurrent.ScheduledFuture;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.InstanceType;
import com.l2jmobius.gameserver.instancemanager.WalkingManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventDispatcher;
import com.l2jmobius.gameserver.model.events.impl.character.npc.OnNpcCreatureSee;
import com.l2jmobius.gameserver.network.serverpackets.MoveToLocation;

public class NpcKnownList extends CharKnownList
{
	private ScheduledFuture<?> _trackingTask = null;
	
	public NpcKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		
		if (getActiveObject().isNpc() && (object instanceof L2Character))
		{
			// Broadcast correct walking NPC position.
			if (object.isPlayer() && getActiveChar().isMoving() && !getActiveChar().isInCombat())
			{
				((L2Character) object).broadcastPacket(new MoveToLocation(getActiveChar()));
			}
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnNpcCreatureSee(getActiveChar(), (L2Character) object, object.isSummon()), getActiveChar());
		}
		return true;
	}
	
	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object.isPlayable())
		{
			return 1500;
		}
		
		return 500;
	}
	
	// Support for Walking monsters aggro
	public void startTrackingTask()
	{
		if ((_trackingTask == null) && (getActiveChar().getAggroRange() > 0))
		{
			_trackingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new TrackingTask(), 2000, 2000);
		}
	}
	
	// Support for Walking monsters aggro
	public void stopTrackingTask()
	{
		if (_trackingTask == null)
		{
			return;
		}
		_trackingTask.cancel(true);
		_trackingTask = null;
	}
	
	// Support for Walking monsters aggro
	protected class TrackingTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getActiveChar().isAttackable())
			{
				return;
			}
			
			final L2Attackable monster = (L2Attackable) getActiveChar();
			if (monster.getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				return;
			}
			
			for (L2PcInstance pl : getKnownPlayers().values())
			{
				if (!pl.isDead() && !pl.isInvul() && pl.isInsideRadius(monster, monster.getAggroRange(), true, false) && (monster.isMonster() || (monster.isInstanceTypes(InstanceType.L2GuardInstance) && (pl.getReputation() < 0))))
				{
					// Send aggroRangeEnter
					if (monster.getHating(pl) == 0)
					{
						monster.addDamageHate(pl, 0, 0);
					}
					
					// Skip attack for other targets, if one is already chosen for attack
					if ((monster.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK) && !monster.isCoreAIDisabled())
					{
						WalkingManager.getInstance().stopMoving(getActiveChar(), false, true);
						monster.addDamageHate(pl, 0, 100);
						monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pl, null);
					}
				}
			}
		}
	}
}
