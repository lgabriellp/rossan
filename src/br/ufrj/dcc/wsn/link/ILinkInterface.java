package br.ufrj.dcc.wsn.link;

public interface ILinkInterface {

	public PacketReader getReader();

	public PacketWriter getWriter();

	public boolean flush();

}