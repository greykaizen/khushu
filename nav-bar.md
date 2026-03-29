NavBar — How It Works                                                                                                                                              
                                                                                                                                                                           
  Shape                                                                                                                                                                    
                                                                                                                                                                           
  Floating pill-shaped surface, centered horizontally, lifted 30.dp from the bottom. Frosted glass look: surfaceVariant at 92% alpha, tonalElevation = 6.dp,               
  shadowElevation = 16.dp, thin white border at 12% alpha.
                                                                                                                                                                           
  Tab Items (PillNavItem)

  Each tab shoots between icon and label on selection:                                                                                                                     
  - Unselected → shows icon (28.dp, muted color)
  - Selected → shows text label (Be Vietnam Pro Medium, primary color)                                                                                                     
  - Transition: AnimatedContent with slideInVertically + fadeIn/Out — slides up when selecting, slides down when deselecting (the "shooter" effect)
  - Selected tab gets a subtle primary-colored background pill (alpha = 0.12f)                                                                                             
  - Horizontal padding animates: 18.dp → 28.dp when selected (pill expands)                                                                                                
                                                                                                                                                                           
  Sliding Indicator                                                                                                                                                        
                                                                                                                                                                           
  A single 25.dp × 3.dp rounded bar that slides across all tabs via spring animation:                                                                                      
  - Tracks each tab's center X in root coordinates using onGloballyPositioned
  - Animates with DampingRatioLowBouncy + StiffnessLow spring (has bounce)                                                                                                 
  - Positioned absolutely at the bottom of the surface via offset { IntOffset(...) }
                                                                                                                                                                           
  AppDestinations enum                                                                                                                                                     
                                                                                                                                                                           
  enum class AppDestinations(val label: String, val icon: Int) {                                                                                                           
      SALAH("Salah", R.drawable.ic_salah),                                                                                                                                 
      TASBEEH("Tasbeeh", R.drawable.ic_tasbeeh),
      LEARN("Learn", R.drawable.ic_learn),                                                                                                                                 
  }
                                                                                                                                                                           
  Key Dependencies

  - androidx.compose.animation (AnimatedContent, animateFloatAsState, animateDpAsState)                                                                                    
  - No third-party nav library — pure Compose + rememberSaveable state in the host
