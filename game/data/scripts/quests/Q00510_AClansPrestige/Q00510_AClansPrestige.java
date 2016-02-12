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
package quests.Q00510_AClansPrestige;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * A Clan's Prestige (510)
 * @author Adry_85
 */
public class Q00510_AClansPrestige extends Quest
{
	// NPC
	private static final int VALDIS = 31331;
	// Quest Item
	private static final int TYRANNOSAURUS_CLAW = 8767;
	
	private static final int[] MOBS =
	{
		22215,
		22216,
		22217
	};
	
	public Q00510_AClansPrestige()
	{
		super(510, Q00510_AClansPrestige.class.getSimpleName(), "A Clan's Prestige");
		addStartNpc(VALDIS);
		addTalkId(VALDIS);
		addKillId(MOBS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31331-3.html":
				st.startQuest();
				break;
			case "31331-6.html":
				st.exitQuest(true, true);
				break;
		}
		return event;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player.getClan() == null)
		{
			return null;
		}
		
		QuestState st = null;
		if (player.isClanLeader())
		{
			st = getQuestState(player, false);
		}
		else
		{
			final L2PcInstance pleader = player.getClan().getLeader().getPlayerInstance();
			if ((pleader != null) && player.isInsideRadius(pleader, 1500, true, false))
			{
				st = getQuestState(pleader, false);
			}
		}
		
		if ((st != null) && st.isStarted())
		{
			st.rewardItems(TYRANNOSAURUS_CLAW, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		final L2Clan clan = player.getClan();
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = ((clan == null) || !player.isClanLeader() || (clan.getLevel() < 5)) ? "31331-0.htm" : "31331-1.htm";
				break;
			case State.STARTED:
				if ((clan == null) || !player.isClanLeader())
				{
					st.exitQuest(true);
					return "31331-8.html";
				}
				
				if (!st.hasQuestItems(TYRANNOSAURUS_CLAW))
				{
					htmltext = "31331-4.html";
				}
				else
				{
					final int count = (int) st.getQuestItemsCount(TYRANNOSAURUS_CLAW);
					final int reward = (count < 10) ? (30 * count) : (59 + (30 * count));
					st.playSound(QuestSound.ITEMSOUND_QUEST_FANFARE_1);
					st.takeItems(TYRANNOSAURUS_CLAW, -1);
					clan.addReputationScore(reward, true);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINT_S_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION).addInt(reward));
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
					htmltext = "31331-7.html";
				}
				break;
			default:
				break;
		}
		return htmltext;
	}
}
