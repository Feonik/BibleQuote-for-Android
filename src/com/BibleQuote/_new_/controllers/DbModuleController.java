package com.BibleQuote._new_.controllers;

import java.util.ArrayList;
import java.util.TreeMap;

import com.BibleQuote._new_.dal.DbLibraryUnitOfWork;
import com.BibleQuote._new_.dal.repository.IModuleRepository;
import com.BibleQuote._new_.managers.EventManager;
import com.BibleQuote._new_.models.DbModule;
import com.BibleQuote._new_.models.Module;

public class DbModuleController implements IModuleController {
	private final String TAG = "DbModuleController";
	
	//private EventManager eventManager;
	private IModuleRepository<Long, DbModule> mRepository;
	
    public DbModuleController(DbLibraryUnitOfWork unit, EventManager eventManager)
    {
		//this.eventManager = eventManager;
		mRepository = unit.getModuleRepository();    	
    }
    
    
    /**
     * @return Возвращает коллекцию модулей с ключом по Module.ShortName
     */
	public TreeMap<String, Module> loadModules() {
		android.util.Log.i(TAG, "Loading modules from a DB storage.");
		TreeMap<String, Module> result = new TreeMap<String, Module>();
		
		ArrayList<Module> moduleList = new ArrayList<Module>();
		moduleList.addAll(mRepository.loadModules());
		for (Module module : moduleList) {
			result.put(module.ShortName, module);
		}
		
		return result;
	}
	

	@Override
	public void loadModulesAsync() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public TreeMap<String, Module> getModules() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Module getModule(String moduleShortName) {
		return mRepository.getModuleByShortName(moduleShortName);
	}

}