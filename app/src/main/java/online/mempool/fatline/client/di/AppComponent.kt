package online.mempool.fatline.client.di

import com.squareup.anvil.annotations.MergeComponent
import dagger.Component
import online.mempool.fatline.data.di.AppScope

@MergeComponent(AppScope::class)
interface AppComponent