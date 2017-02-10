package org.gaofamily.libpostal.service;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public abstract class AddressServiceFactory {
    private static AddressService _instance;

    public static AddressService getAddressService() {
        if (_instance == null) {
            synchronized (AddressServiceFactory.class) {
                if (_instance == null) {
                    _instance = new AddressServiceImpl();
                }
            }
        }
        return _instance;
    }
}
