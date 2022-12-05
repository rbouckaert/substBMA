package beast.core;

import beast.base.core.Description;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Derived classes will reuse objects that have been removed from the list.")
public interface Recycle {
    public int getObjectCreatedCount();
}
