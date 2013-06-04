package br.ufrj.dcc.wsn.link;

import br.ufrj.dcc.wsn.util.Logger;

public interface ILinkInterface {
	public PacketReader getReader();

	public PacketWriter getWriter();

	public boolean flush();

	public long getAddress();

	public Logger getLog();

}