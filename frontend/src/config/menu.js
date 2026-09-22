import menuCodeList from './menuCode.json'

const menuConfigMap = new Map()

function collectMenuConfig(menuList) {
  menuList.forEach(menu => {
    menuConfigMap.set(menu.code, menu)
    collectMenuConfig(menu.children || [])
  })
}

collectMenuConfig(menuCodeList)

export const MENU_CODE = Object.freeze({
  ADMIN: 'admin',
  WRONG_QUESTION: Object.freeze({
    CREATE: 'wrongQuestion-create',
    DETAIL: 'wrongQuestion-detail',
    UPDATE: 'wrongQuestion-update',
    UPDATE_IMAGE: 'wrongQuestion-updateImage',
    IMPORT: 'wrongQuestion-import',
    EXPORT: 'wrongQuestion-export',
    DELETE: 'wrongQuestion-delete',
    BATCH_DELETE: 'wrongQuestion-batchDelete'
  }),
  BOOK: Object.freeze({
    CREATE: 'book-create',
    DETAIL: 'book-detail',
    UPDATE: 'book-update',
    DELETE: 'book-delete'
  }),
  HOME_WORK: Object.freeze({
    CREATE: 'homeWork-create',
    DETAIL: 'homeWork-detail',
    UPDATE: 'homeWork-update',
    DELETE: 'homeWork-delete'
  })
})

export function buildSidebarMenus(menuTree) {
  const menus = (menuTree || []).reduce((sidebarMenus, backendMenu) => {
    const config = menuConfigMap.get(backendMenu.code)
    if (!config) {
      return sidebarMenus
    }
    const children = buildSidebarMenus(backendMenu.children)
    if (!config.path && children.length === 0) {
      return sidebarMenus
    }
    sidebarMenus.push({
      code: backendMenu.code,
      title: config.name || backendMenu.menuName,
      path: config.path || '',
      icon: config.icon || '',
      children
    })
    return sidebarMenus
  }, [])

  const dashboardIndex = menus.findIndex(menu => menu.code === 'dashboard')
  if (dashboardIndex > 0) {
    const dashboard = menus.splice(dashboardIndex, 1)[0]
    menus.unshift(dashboard)
  }
  return menus
}

export function getFirstMenuPath(menuList) {
  for (const menu of menuList) {
    if (menu.path) {
      return menu.path
    }
    const childPath = getFirstMenuPath(menu.children || [])
    if (childPath) {
      return childPath
    }
  }
  return ''
}

export function hasMenuPath(menuList, path) {
  return menuList.some(menu => menu.path === path || hasMenuPath(menu.children || [], path))
}
