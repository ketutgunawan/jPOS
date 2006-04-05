/*
 * $Log$
 * Revision 1.11  2000/06/21 21:34:00  apr
 * Added PersistentEngine get/set
 *
 * Revision 1.10  2000/05/24 12:22:26  apr
 * Added no-arg constructor (required by QSP)
 *
 * Revision 1.9  2000/04/16 23:53:06  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.8  2000/04/16 22:41:07  victor
 * added packager.* import
 *
 * Revision 1.7  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.6  2000/02/01 12:50:35  apr
 * Added dummy cancel
 *
 * Revision 1.5  2000/01/30 23:33:50  apr
 * CVS sync/backup - intermediate version
 *
 * Revision 1.4  2000/01/20 23:02:45  apr
 * Adding FinancialTransaction support - CVS sync
 *
 * Revision 1.3  2000/01/11 01:24:39  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.2  1999/12/06 01:19:08  apr
 * CVS snapshot
 *
 * Revision 1.1  1999/11/26 12:16:45  apr
 * CVS devel snapshot
 *
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

import org.jpos.iso.*;
import org.jpos.iso.packager.*;
import org.jpos.util.*;
import org.jpos.tpl.PersistentEngine;

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 */
public abstract class CardAgentBase implements CardAgent, LogSource {
    protected Sequencer seq;
    protected Configuration cfg;
    protected String realm;
    protected Logger logger;
    protected ISOPackager imagePackager;
    protected PersistentEngine engine;

    /**
     * no args constructor
     */
    public CardAgentBase () {
	setLogger (logger, realm);
	populateSelector();
	imagePackager = new ISO87BPackager();	// default, can be changed
    }

    /**
     * @param cfg Configuration provider
     * @param seq Sequencer provider
     * @param logger Logger
     * @param realm  Logger's realm
     */
    public CardAgentBase
	(Configuration cfg, Sequencer seq, Logger logger, String realm)
    {
	this.cfg = cfg;
	this.seq = seq;
	setLogger (logger, realm);
	populateSelector();
	imagePackager = new ISO87BPackager();	// default, can be changed
    }
    public abstract int getID();
    protected abstract void populateSelector();
    public abstract String getPropertyPrefix();

    public void setSequencer (Sequencer seq) {
	this.seq = seq;
    }
    public Sequencer getSequencer () {
	return seq;
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
    /**
     * @param packager default internal image packager
     */
    public void setImagePackager (ISOPackager packager) {
	imagePackager = packager;
    }
    /**
     * @return default Image packager
     */
    public ISOPackager getImagePackager() {
	return imagePackager;
    }
    public Configuration getConfiguration() {
	return cfg;
    }
    public boolean canHandle (CardTransaction t) {
	String action = t.getAction();
	try {
	    Class[] paramTemplate = { CardTransaction.class };

	    Method method = getClass().getMethod(action, paramTemplate);
	    method = getClass().getMethod("isValid_" + action, paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    return ((Boolean) method.invoke (this, param)).booleanValue();
	} catch (Exception ex) { 
	    Logger.log (new LogEvent (this, "canHandle", ex));
	} 
	return false;
    }

    /**
     * Process CardTransaction
     * @param t CardTransaction
     * @return CardTransactionResponse
     */
    public CardTransactionResponse process (CardTransaction t) 
	throws CardAgentException
    {
	String action = t.getAction();
	try {
	    Class[] paramTemplate = { CardTransaction.class };
	    Method method = getClass().getMethod(action, paramTemplate);
	    Object[] param = new Object[1];
	    param[0] = t;
	    return (CardTransactionResponse) method.invoke (this, param);
	} catch (InvocationTargetException e) {
	    throw new CardAgentException ((Exception) e.getTargetException());
	} catch (Exception e) { 
	    Logger.log (new LogEvent (this, "process", e));
	    throw new CardAgentException (e);
	} 
    }

    public CardTransactionResponse cancel  (byte[] image) 
	throws CardAgentException
    {
	throw new CardAgentException ("not implemented");
    }
    public abstract CardTransactionResponse getResponse (byte[] b) 
	throws CardAgentException;

    public void setPersistentEngine (PersistentEngine engine) {
	this.engine = engine;
    }
    public PersistentEngine getPersistentEngine() {
	return engine;
    }
}